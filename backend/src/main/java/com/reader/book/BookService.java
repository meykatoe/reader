package com.reader.book;

import com.reader.common.ApiException;
import com.reader.storage.FileStorage;
import com.reader.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookService {

    private static final int MAX_TITLE_LENGTH = 255;

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    public BookService(BookRepository bookRepository,
                       UserRepository userRepository,
                       FileStorage fileStorage) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
    }

    public List<Book> list(Long userId, boolean mineOnly) {
        return mineOnly ? bookRepository.findOwned(userId) : bookRepository.findVisible(userId);
    }

    // 權限檢查
    public Book getVisible(Long bookId, Long userId) {
        return bookRepository.findWithOwner(bookId)
                .filter(book -> book.isVisibleTo(userId))
                .orElseThrow(() -> ApiException.notFound("找不到書籍"));
    }

    public Book changeVisibility(Long bookId, Long userId, boolean privateBook) {
        Book book = getOwned(bookId, userId);
        book.changeVisibility(privateBook);
        bookRepository.save(book);
        return getVisible(bookId, userId);
    }

    public void delete(Long bookId, Long userId) {
        Book book = getOwned(bookId, userId);
        bookRepository.delete(book);
        fileStorage.delete(book.getStorageKey());
    }

    private Book getOwned(Long bookId, Long userId) {
        Book book = getVisible(bookId, userId);
        if (!book.isOwnedBy(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "僅擁有者可執行此操作");
        }
        return book;
    }

    public Book upload(Long userId, MultipartFile file, String title, boolean privateBook) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("檔案內容為空");
        }
        BookFormat format = BookFormat.fromFilename(file.getOriginalFilename());
        String key = UUID.randomUUID().toString();
        try {
            fileStorage.saveSource(key, format.extension(), file);
            Book book = new Book(
                    userRepository.getReferenceById(userId),
                    resolveTitle(title, file.getOriginalFilename()),
                    null,
                    format,
                    privateBook,
                    key,
                    file.getSize());
            Long id = bookRepository.save(book).getId();
            return bookRepository.findWithOwner(id).orElseThrow();
        } catch (RuntimeException ex) {
            // 清除殘留檔
            fileStorage.delete(key);
            throw ex;
        }
    }

    private static String resolveTitle(String title, String filename) {
        String resolved = StringUtils.hasText(title)
                ? title.strip()
                : StringUtils.stripFilenameExtension(StringUtils.getFilename(filename));
        return resolved.length() > MAX_TITLE_LENGTH ? resolved.substring(0, MAX_TITLE_LENGTH) : resolved;
    }
}
