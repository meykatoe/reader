package com.reader.admin.book;

import com.reader.admin.AdminPaging;
import com.reader.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {

    private final AdminBookService adminBookService;

    public AdminBookController(AdminBookService adminBookService) {
        this.adminBookService = adminBookService;
    }

    @GetMapping
    public PageResponse<AdminBookResponse> list(@RequestParam(defaultValue = "") String keyword,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                adminBookService.search(keyword.strip(), AdminPaging.newestFirst(page, size)),
                AdminBookResponse::of);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AdminBookResponse create(@RequestPart("file") MultipartFile file,
                                    @RequestParam(required = false) String title,
                                    @RequestParam(defaultValue = "false") boolean isPrivate) {
        return AdminBookResponse.of(adminBookService.create(file, title, isPrivate));
    }

    @PutMapping("/{id}")
    public AdminBookResponse update(@PathVariable Long id,
                                    @Valid @RequestBody AdminBookUpdateRequest request) {
        return AdminBookResponse.of(adminBookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        adminBookService.delete(id);
    }
}
