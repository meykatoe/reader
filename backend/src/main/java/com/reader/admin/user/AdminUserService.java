package com.reader.admin.user;

import com.reader.book.Book;
import com.reader.book.BookRepository;
import com.reader.book.OwnerBookCount;
import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.user.User;
import com.reader.user.UserRepository;
import java.time.Instant;
import java.util.Map;
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
    private final TransactionTemplate transaction;

    public AdminUserService(UserRepository userRepository,
                            BookRepository bookRepository,
                            TransactionTemplate transaction) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.transaction = transaction;
    }

    public Page<AdminUserResponse> search(String keyword, Pageable pageable) {
        Page<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
        Map<Long, Long> counts = bookRepository
                .countByOwners(users.map(User::getId).getContent())
                .stream()
                .collect(Collectors.toMap(OwnerBookCount::getOwnerId, OwnerBookCount::getTotal));
        return users.map(user ->
                AdminUserResponse.of(user, counts.getOrDefault(user.getId(), 0L)));
    }

    public AdminUserResponse changeMembership(Long userId, Instant memberUntil) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        user.changeMembership(memberUntil);
        userRepository.save(user);
        return AdminUserResponse.of(user, bookRepository.findOwned(userId).size());
    }

    // 書籍保留不刪
    public void delete(Long userId) {
        try {
            transaction.executeWithoutResult(status -> {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
                bookRepository.findOwned(userId).forEach(Book::detachOwner);
                // 先解除關聯
                bookRepository.flush();
                userRepository.delete(user);
            });
        } catch (DataIntegrityViolationException ex) {
            // 同時有上傳
            throw new ApiException(ErrorCode.USER_DATA_CHANGED);
        }
    }
}
