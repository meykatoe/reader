package com.reader.book;

import com.reader.common.ApiException;
import com.reader.parser.ParsedBook;
import com.reader.parser.ParserService;
import com.reader.storage.FileStorage;
import com.reader.user.UserRepository;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookService {

    private static final int MAX_TEXT_LENGTH = 255;

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;
    private final ParserService parserService;
    private final BookImporter bookImporter;

    public BookService(BookRepository bookRepository,
                       UserRepository userRepository,
                       FileStorage fileStorage,
                       ParserService parserService,
                       BookImporter bookImporter) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
        this.parserService = parserService;
        this.bookImporter = bookImporter;
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
            Path source = fileStorage.saveSource(key, format.extension(), file);
            ParsedBook parsed = parserService.parse(format, source);
            Book book = new Book(
                    userRepository.getReferenceById(userId),
                    resolveTitle(title, parsed.title(), file.getOriginalFilename()),
                    truncate(parsed.author()),
                    format,
                    privateBook,
                    key,
                    file.getSize());
            book.assignCover(parsed.coverName());
            fileStorage.saveAssets(key, parsed.assets());
            Long id = bookImporter.save(book, parsed.chapters());
            return bookRepository.findWithOwner(id).orElseThrow();
        } catch (RuntimeException ex) {
            // 清除殘留檔
            fileStorage.delete(key);
            throw ex;
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
