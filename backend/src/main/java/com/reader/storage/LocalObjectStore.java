package com.reader.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileSystemUtils;

public class LocalObjectStore implements ObjectStore {

    private final Path root;

    public LocalObjectStore(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public void put(String key, Path source) {
        try {
            Files.copy(source, prepare(key), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public void put(String key, byte[] data) {
        try {
            Files.write(prepare(key), data);
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public Optional<Resource> get(String key) {
        Path path = resolve(key);
        return Files.isRegularFile(path) ? Optional.of(new FileSystemResource(path)) : Optional.empty();
    }

    @Override
    public void deleteDirectory(String prefix) {
        try {
            FileSystemUtils.deleteRecursively(resolve(prefix));
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    private Path prepare(String key) throws IOException {
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        return target;
    }

    // 禁止跳出根目錄
    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root) || path.equals(root)) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return path;
    }
}
