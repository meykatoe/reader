package com.reader.book;

import com.reader.book.dto.BookResponse;
import com.reader.book.dto.VisibilityRequest;
import com.reader.common.Result;
import com.reader.user.AuthUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping
    public Result<List<BookResponse>> list(@AuthenticationPrincipal AuthUser user,
                                           @RequestParam(defaultValue = "false") boolean mine) {
        return Result.ok(bookService.list(user.id(), mine).stream()
                .map(book -> BookResponse.of(book, user.id()))
                .toList());
    }

    @GetMapping("/{id}")
    public Result<BookResponse> get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return Result.ok(BookResponse.of(bookService.getVisible(id, user.id()), user.id()));
    }

    @PatchMapping("/{id}")
    public Result<BookResponse> update(@AuthenticationPrincipal AuthUser user,
                                       @PathVariable Long id,
                                       @Valid @RequestBody VisibilityRequest request) {
        Book book = bookService.changeVisibility(id, user.id(), request.isPrivate());
        return Result.ok(BookResponse.of(book, user.id()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        bookService.delete(id, user.id());
        return Result.ok();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Result<BookResponse> upload(@AuthenticationPrincipal AuthUser user,
                                       @RequestPart("file") MultipartFile file,
                                       @RequestParam(required = false) String title,
                                       @RequestParam(defaultValue = "false") boolean isPrivate) {
        Book book = bookService.upload(user.id(), file, title, isPrivate);
        return Result.ok(BookResponse.of(book, user.id()));
    }
}
