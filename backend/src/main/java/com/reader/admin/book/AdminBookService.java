package com.reader.admin.book;

import com.reader.book.Book;
import com.reader.book.BookRepository;
import com.reader.book.BookService;
import com.reader.book.DeletionCause;
import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
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

    public Page<Book> search(String keyword, boolean deleted, Pageable pageable) {
        return bookRepository.search(keyword, deleted, pageable);
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
        bookService.softDelete(bookService.get(bookId), DeletionCause.ADMIN);
    }

    public Book restore(Long bookId) {
        Book book = bookRepository.findDeleted(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        book.restore();
        bookRepository.save(book);
        return bookService.get(bookId);
    }
}
