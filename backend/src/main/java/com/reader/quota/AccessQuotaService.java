package com.reader.quota;

import com.reader.book.Book;
import com.reader.limit.LimitProperties;
import com.reader.limit.TooManyRequestsException;
import com.reader.user.User;
import com.reader.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

// 每日額度
@Service
public class AccessQuotaService {

    private final BookAccessRepository accessRepository;
    private final UserRepository userRepository;
    private final LimitProperties limits;
    private final Clock clock;
    private final TransactionTemplate transaction;

    public AccessQuotaService(BookAccessRepository accessRepository,
                              UserRepository userRepository,
                              LimitProperties limits,
                              Clock clock,
                              PlatformTransactionManager transactionManager) {
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
        this.limits = limits;
        this.clock = clock;
        this.transaction = new TransactionTemplate(transactionManager);
        // 讀最新次數
        this.transaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    // 本人不限
    public boolean isGranted(Book book, Long userId, AccessKind kind) {
        return book.isOwnedBy(userId) || recorded(userId, kind, today(), book.getId());
    }

    public void grant(Book book, Long userId, AccessKind kind) {
        if (isGranted(book, userId, kind)) {
            return;
        }
        LocalDate date = today();
        transaction.executeWithoutResult(status -> {
            // 同帳號排隊
            User user = userRepository.lockById(userId).orElseThrow();
            if (recorded(userId, kind, date, book.getId())) {
                return;
            }
            int limit = limitOf(user, kind);
            if (accessRepository.countByUserIdAndKindAndAccessDate(userId, kind, date) >= limit) {
                throw exceeded(kind, limit);
            }
            accessRepository.save(
                    new BookAccess(userId, book.getId(), kind, date, clock.instant()));
        });
    }

    public QuotaResponse downloadStatus(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Long> downloaded =
                accessRepository.findBookIds(userId, AccessKind.DOWNLOAD, today());
        return new QuotaResponse(
                user.isMember(clock.instant()),
                user.getMemberUntil(),
                limitOf(user, AccessKind.DOWNLOAD),
                downloaded.size(),
                downloaded);
    }

    private boolean recorded(Long userId, AccessKind kind, LocalDate date, Long bookId) {
        return accessRepository.existsByUserIdAndKindAndAccessDateAndBookId(
                userId, kind, date, bookId);
    }

    private int limitOf(User user, AccessKind kind) {
        if (kind == AccessKind.READ) {
            return limits.dailyReadBooks();
        }
        Instant now = clock.instant();
        return user.isMember(now) ? limits.memberDownloads() : limits.freeDownloads();
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), limits.zone());
    }

    private static TooManyRequestsException exceeded(AccessKind kind, int limit) {
        String action = kind == AccessKind.DOWNLOAD ? "下載" : "開啟";
        return new TooManyRequestsException(
                "今日可" + action + "的書籍已達上限（" + limit + " 本），明天再試");
    }
}
