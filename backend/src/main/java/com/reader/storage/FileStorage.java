package com.reader.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
@EnableConfigurationProperties(StorageProperties.class)
public class FileStorage {

    private final Path root;

    public FileStorage(StorageProperties properties) {
        this.root = properties.root().toAbsolutePath().normalize();
    }

    public Path saveSource(String key, String extension, MultipartFile file) {
        Path target = sourcePath(key, extension);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return target;
    }

    public Path sourcePath(String key, String extension) {
        return bookDirectory(key).resolve("source." + extension);
    }

    public void saveAssets(String key, Map<String, byte[]> assets) {
        try {
            Path directory = Files.createDirectories(bookDirectory(key).resolve("assets"));
            for (Map.Entry<String, byte[]> asset : assets.entrySet()) {
                Files.write(directory.resolve(asset.getKey()), asset.getValue());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    // 名稱須先驗證
    public Path assetPath(String key, String name) {
        return bookDirectory(key).resolve("assets").resolve(name);
    }

    public void delete(String key) {
        try {
            FileSystemUtils.deleteRecursively(bookDirectory(key));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Path bookDirectory(String key) {
        return root.resolve("books").resolve(key);
    }
}
