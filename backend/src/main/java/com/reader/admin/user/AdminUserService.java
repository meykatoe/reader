package com.reader.admin.user;

import com.reader.book.Book;
import com.reader.book.BookRepository;
import com.reader.book.DeletionCause;
import com.reader.book.OwnerBookCount;
import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.user.User;
import com.reader.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final Clock clock;

    public AdminUserService(UserRepository userRepository,
                            BookRepository bookRepository,
                            Clock clock) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.clock = clock;
    }

    public Page<AdminUserResponse> search(String keyword, boolean deleted, Pageable pageable) {
        Page<User> users = deleted
                ? userRepository.findByUsernameContainingIgnoreCaseAndDeletedAtIsNotNull(
                        keyword, pageable)
                : userRepository.findByUsernameContainingIgnoreCaseAndDeletedAtIsNull(
                        keyword, pageable);
        Map<Long, Long> counts = bookRepository
                .countByOwners(users.map(User::getId).getContent())
                .stream()
                .collect(Collectors.toMap(OwnerBookCount::getOwnerId, OwnerBookCount::getTotal));
        return users.map(user ->
                AdminUserResponse.of(user, counts.getOrDefault(user.getId(), 0L)));
    }

    @Transactional
    public AdminUserResponse changeMembership(Long userId, Instant memberUntil) {
        User user = find(userId, existing -> !existing.isDeleted());
        user.changeMembership(memberUntil);
        return respond(user);
    }

    // 私密書同刪
    @Transactional
    public AdminUserResponse delete(Long userId) {
        User user = find(userId, existing -> !existing.isDeleted());
        Instant now = clock.instant();
        bookRepository.findOwned(userId).stream()
                .filter(Book::isPrivateBook)
                .forEach(book -> book.softDelete(DeletionCause.ACCOUNT, now));
        user.softDelete(now);
        return respond(user);
    }

    // 僅復原同刪書
    @Transactional
    public AdminUserResponse restore(Long userId) {
        User user = find(userId, User::isDeleted);
        user.restore();
        bookRepository.findByOwnerIdAndDeletionCause(userId, DeletionCause.ACCOUNT)
                .forEach(Book::restore);
        return respond(user);
    }

    private User find(Long userId, Predicate<User> state) {
        return userRepository.findById(userId)
                .filter(state)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    private AdminUserResponse respond(User user) {
        return AdminUserResponse.of(user, bookRepository.findOwned(user.getId()).size());
    }
}
