package com.reader.maintenance;

import com.reader.storage.BookStorage;
import com.reader.storage.ObjectStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;

// 只複製不刪除
class StorageMigration {

    private final Path root;
    private final ObjectStore target;

    StorageMigration(Path root, ObjectStore target) {
        this.root = root.toAbsolutePath().normalize();
        this.target = target;
    }

    record BookFiles(String key, String sourceExtension) {
    }

    record Result(int copied, int skipped, List<String> failures) {

        boolean succeeded() {
            return failures.isEmpty();
        }
    }

    Result migrate(List<BookFiles> books) {
        int copied = 0;
        int skipped = 0;
        List<String> failures = new ArrayList<>();
        for (BookFiles book : books) {
            for (Path file : filesOf(book, failures)) {
                String key = root.relativize(file).toString().replace('\\', '/');
                try {
                    if (matches(key, file)) {
                        skipped++;
                        continue;
                    }
                    target.put(key, file);
                    // 上傳後核對
                    if (matches(key, file)) {
                        copied++;
                    } else {
                        failures.add(key + "：大小不符");
                    }
                } catch (IOException | RuntimeException ex) {
                    failures.add(key + "：" + ex.getMessage());
                }
            }
        }
        return new Result(copied, skipped, failures);
    }

    private List<Path> filesOf(BookFiles book, List<String> failures) {
        Path directory = root.resolve(BookStorage.directory(book.key()));
        Path source = directory.resolve("source." + book.sourceExtension());
        if (!Files.isRegularFile(source)) {
            failures.add(root.relativize(source) + "：本機缺少原始檔");
        }
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile).sorted().toList();
        } catch (IOException ex) {
            failures.add(root.relativize(directory) + "：" + ex.getMessage());
            return List.of();
        }
    }

    private boolean matches(String key, Path file) throws IOException {
        Resource existing = target.get(key).orElse(null);
        return existing != null && existing.contentLength() == Files.size(file);
    }
}
