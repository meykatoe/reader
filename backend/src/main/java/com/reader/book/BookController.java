package com.reader.book;

import com.reader.book.dto.BookResponse;
import com.reader.user.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse upload(@AuthenticationPrincipal AuthUser user,
                               @RequestPart("file") MultipartFile file,
                               @RequestParam(required = false) String title,
                               @RequestParam(defaultValue = "false") boolean isPrivate) {
        return BookResponse.of(bookService.upload(user.id(), file, title, isPrivate), user.id());
    }
}
