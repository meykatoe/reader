package com.reader.book;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import com.reader.parser.ParsedBook;
import com.reader.parser.ParserService;
import com.reader.storage.BookStorage;
import com.reader.user.UserRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private static final int MAX_TEXT_LENGTH = 255;

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookStorage bookStorage;
    private final ParserService parserService;
    private final BookImporter bookImporter;
    private final Clock clock;

    public BookService(BookRepository bookRepository,
                       UserRepository userRepository,
                       BookStorage bookStorage,
                       ParserService parserService,
                       BookImporter bookImporter,
                       Clock clock) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookStorage = bookStorage;
        this.parserService = parserService;
        this.bookImporter = bookImporter;
        this.clock = clock;
    }

    public List<Book> list(Long userId, boolean mineOnly) {
        return mineOnly ? bookRepository.findOwned(userId) : bookRepository.findVisible(userId);
    }

    // 權限檢查
    public Book getVisible(Long bookId, Long userId) {
        return bookRepository.findActive(bookId)
                .filter(book -> book.isVisibleTo(userId))
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }

    public Book changeVisibility(Long bookId, Long userId, boolean privateBook) {
        Book book = getOwned(bookId, userId);
        book.changeVisibility(privateBook);
        bookRepository.save(book);
        return getVisible(bookId, userId);
    }

    public Book get(Long bookId) {
        return bookRepository.findActive(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
    }

    public void delete(Long bookId, Long userId) {
        softDelete(getOwned(bookId, userId), DeletionCause.OWNER);
    }

    // 須先驗權限
    public void softDelete(Book book, DeletionCause cause) {
        book.softDelete(cause, clock.instant());
        bookRepository.save(book);
    }

    private Book getOwned(Long bookId, Long userId) {
        Book book = getVisible(bookId, userId);
        if (!book.isOwnedBy(userId)) {
            throw new ApiException(ErrorCode.BOOK_NOT_OWNER);
        }
        return book;
    }

    // 空值為平臺
    public Book upload(Long ownerId, MultipartFile file, String title, boolean privateBook) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCode.FILE_EMPTY);
        }
        BookFormat format = BookFormat.fromFilename(file.getOriginalFilename());
        String key = UUID.randomUUID().toString();
        try (UploadedFile upload = UploadedFile.of(file, format.extension())) {
            ParsedBook parsed = parserService.parse(format, upload.path());
            Book book = new Book(
                    ownerId == null ? null : userRepository.getReferenceById(ownerId),
                    resolveTitle(title, parsed.title(), file.getOriginalFilename()),
                    truncate(parsed.author()),
                    format,
                    privateBook,
                    key,
                    file.getSize());
            book.assignCover(parsed.coverName());
            book.assignPageCount(parsed.pageCount());
            Long id = store(book, upload, parsed);
            return bookRepository.findActive(id).orElseThrow();
        }
    }

    private Long store(Book book, UploadedFile upload, ParsedBook parsed) {
        try {
            bookStorage.save(book.getStorageKey(), book.getFormat().extension(),
                    upload.path(), parsed.assets());
            return bookImporter.save(book, parsed.chapters());
        } catch (RuntimeException ex) {
            // 清除殘留檔
            discardFiles(book.getStorageKey());
            throw ex;
        }
    }

    // 僅清失敗上傳
    private void discardFiles(String key) {
        try {
            bookStorage.delete(key);
        } catch (RuntimeException ex) {
            log.warn("Orphaned files left in storage: {}", BookStorage.directory(key), ex);
        }
    }

    private static String resolveTitle(String input, String parsed, String filename) {
        if (StringUtils.hasText(input)) {
            return truncate(input);
        }
        if (StringUtils.hasText(parsed)) {
            return truncate(parsed);
        }
        return truncate(StringUtils.stripFilenameExtension(StringUtils.getFilename(filename)));
    }

    private static String truncate(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String value = text.strip();
        return value.length() > MAX_TEXT_LENGTH ? value.substring(0, MAX_TEXT_LENGTH) : value;
    }
}
