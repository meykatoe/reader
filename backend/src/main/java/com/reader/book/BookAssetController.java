package com.reader.book;

import com.reader.common.ApiException;
import com.reader.parser.ImageType;
import com.reader.storage.BookStorage;
import com.reader.user.AuthUser;
import java.time.Duration;
import java.util.regex.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books/{id}")
public class BookAssetController {

    // 防路徑穿越
    private static final Pattern ASSET_NAME = Pattern.compile("[a-z]+-?\\d*\\.(jpg|png|gif|webp)");

    private final BookService bookService;
    private final BookStorage bookStorage;

    public BookAssetController(BookService bookService, BookStorage bookStorage) {
        this.bookService = bookService;
        this.bookStorage = bookStorage;
    }

    @GetMapping("/cover")
    public ResponseEntity<Resource> cover(@AuthenticationPrincipal AuthUser user,
                                          @PathVariable Long id) {
        Book book = bookService.getVisible(id, user.id());
        return image(book, book.getCoverName());
    }

    @GetMapping("/assets/{name}")
    public ResponseEntity<Resource> asset(@AuthenticationPrincipal AuthUser user,
                                          @PathVariable Long id,
                                          @PathVariable String name) {
        return image(bookService.getVisible(id, user.id()), name);
    }

    // 支援分段下載
    @GetMapping("/file")
    public ResponseEntity<Resource> file(@AuthenticationPrincipal AuthUser user,
                                         @PathVariable Long id) {
        Book book = bookService.getVisible(id, user.id());
        if (book.getFormat() != BookFormat.PDF) {
            throw ApiException.notFound("僅提供 PDF 原始檔");
        }
        Resource source = bookStorage.source(book.getStorageKey(), book.getFormat().extension())
                .orElseThrow(() -> ApiException.notFound("找不到檔案"));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(source);
    }

    private ResponseEntity<Resource> image(Book book, String name) {
        if (name == null || !ASSET_NAME.matcher(name).matches()) {
            throw ApiException.notFound("找不到圖片");
        }
        Resource asset = bookStorage.asset(book.getStorageKey(), name)
                .orElseThrow(() -> ApiException.notFound("找不到圖片"));
        String mediaType = ImageType.fromName(name).orElseThrow().mediaType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaType))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(asset);
    }
}
