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

    public void delete(Long userId) {
        List<String> storageKeys = deleteRecords(userId);
        // 提交後刪檔
        storageKeys.forEach(bookService::discardFiles);
    }

    private List<String> deleteRecords(Long userId) {
        try {
            return transaction.execute(status -> {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
                List<Book> books = bookRepository.findOwned(userId);
                bookRepository.deleteAll(books);
                bookRepository.flush();
                userRepository.delete(user);
                return books.stream().map(Book::getStorageKey).toList();
            });
        } catch (DataIntegrityViolationException ex) {
            // 同時有上傳
            throw new ApiException(ErrorCode.USER_DATA_CHANGED);
        }
    }
}
