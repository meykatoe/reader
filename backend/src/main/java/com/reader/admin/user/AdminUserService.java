package com.reader.admin.user;

import com.reader.book.Book;
import com.reader.book.BookRepository;
import com.reader.book.BookService;
import com.reader.book.OwnerBookCount;
import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.user.User;
import com.reader.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final TransactionTemplate transaction;

    public AdminUserService(UserRepository userRepository,
                            BookRepository bookRepository,
                            BookService bookService,
                            TransactionTemplate transaction) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.transaction = transaction;
    }

    public Page<AdminUserResponse> search(String keyword, Pageable pageable) {
        Page<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
        Map<Long, OwnerBookCount> counts = bookRepository
                .countByOwners(users.map(User::getId).getContent())
                .stream()
                .collect(Collectors.toMap(OwnerBookCount::getOwnerId, Function.identity()));
        return users.map(user -> {
            OwnerBookCount count = counts.get(user.getId());
            return count == null
                    ? AdminUserResponse.of(user, 0, 0)
                    : AdminUserResponse.of(user, count.getTotal(), count.getPrivateTotal());
        });
    }

    public AdminUserResponse changeMembership(Long userId, Instant memberUntil) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        user.changeMembership(memberUntil);
        userRepository.save(user);
        List<Book> books = bookRepository.findOwned(userId);
        long privateCount = books.stream().filter(Book::isPrivateBook).count();
        return AdminUserResponse.of(user, books.size(), privateCount);
    }

    // 僅保留公開書
    public void delete(Long userId) {
        // 提交後才刪檔
        deleteRecords(userId).forEach(bookService::discardFiles);
    }

    private List<String> deleteRecords(Long userId) {
        try {
            return transaction.execute(status -> {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
                Map<Boolean, List<Book>> byPrivacy = bookRepository.findOwned(userId).stream()
                        .collect(Collectors.partitioningBy(Book::isPrivateBook));
                List<Book> removed = byPrivacy.get(true);
                bookRepository.deleteAll(removed);
                byPrivacy.get(false).forEach(Book::detachOwner);
                // 先解除關聯
                bookRepository.flush();
                userRepository.delete(user);
                return removed.stream().map(Book::getStorageKey).toList();
            });
        } catch (DataIntegrityViolationException ex) {
            // 同時有上傳
            throw new ApiException(ErrorCode.USER_DATA_CHANGED);
        }
    }
}
