package com.reader.admin.book;

import com.reader.book.Book;
import com.reader.book.BookRepository;
import com.reader.book.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

// 不限擁有者
@Service
public class AdminBookService {

    private final BookService bookService;
    private final BookRepository bookRepository;

    public AdminBookService(BookService bookService, BookRepository bookRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }

    public Page<Book> search(String keyword, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    public Book create(MultipartFile file, String title, boolean privateBook) {
        return bookService.upload(null, file, title, privateBook);
    }

    public Book update(Long bookId, AdminBookUpdateRequest request) {
        Book book = bookService.get(bookId);
        String author = StringUtils.hasText(request.author()) ? request.author().strip() : null;
        book.rename(request.title().strip(), author);
        book.changeVisibility(request.isPrivate());
        bookRepository.save(book);
        return bookService.get(bookId);
    }

    public void delete(Long bookId) {
        bookService.remove(bookService.get(bookId));
    }
}
