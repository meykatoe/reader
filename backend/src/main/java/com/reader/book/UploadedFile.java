package com.reader.book;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

// 解析用暫存檔
record UploadedFile(Path path) implements AutoCloseable {

    static UploadedFile of(MultipartFile file, String extension) {
        try {
            Path path = Files.createTempFile("upload-", "." + extension);
            file.transferTo(path);
            return new UploadedFile(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
