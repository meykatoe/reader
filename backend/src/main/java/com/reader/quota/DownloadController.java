package com.reader.quota;

import com.reader.book.Book;
import com.reader.book.BookService;
import com.reader.common.ApiException;
import com.reader.storage.BookStorage;
import com.reader.user.AuthUser;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DownloadController {

    private final BookService bookService;
    private final BookStorage bookStorage;
    private final AccessQuotaService quotaService;

    public DownloadController(BookService bookService,
                              BookStorage bookStorage,
                              AccessQuotaService quotaService) {
        this.bookService = bookService;
        this.bookStorage = bookStorage;
        this.quotaService = quotaService;
    }

    @GetMapping("/quota")
    public QuotaResponse quota(@AuthenticationPrincipal AuthUser user) {
        return quotaService.downloadStatus(user.id());
    }

    // 扣額度
    @PostMapping("/books/{id}/downloads")
    public QuotaResponse request(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        Book book = bookService.getVisible(id, user.id());
        // 缺檔不扣次
        source(book);
        quotaService.grant(book, user.id(), AccessKind.DOWNLOAD);
        return quotaService.downloadStatus(user.id());
    }

    @GetMapping("/books/{id}/download")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal AuthUser user,
                                             @PathVariable Long id) {
        Book book = bookService.getVisible(id, user.id());
        if (!quotaService.isGranted(book, user.id(), AccessKind.DOWNLOAD)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "請先取得下載權限");
        }
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(DownloadFilename.of(book), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(source(book));
    }

    private Resource source(Book book) {
        return bookStorage.source(book.getStorageKey(), book.getFormat().extension())
                .orElseThrow(() -> ApiException.notFound("找不到檔案"));
    }
}
