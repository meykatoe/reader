package com.reader.maintenance;

import static org.assertj.core.api.Assertions.assertThat;

import com.reader.storage.LocalObjectStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageMigrationTest {

    @TempDir
    private Path directory;

    @Test
    void copiesOnceAndKeepsOriginals() throws IOException {
        Path root = directory.resolve("source");
        Path assets = Files.createDirectories(root.resolve("books/a/assets"));
        Path source = Files.writeString(root.resolve("books/a/source.txt"), "內容");
        Files.writeString(assets.resolve("img-1.png"), "png");
        LocalObjectStore target = new LocalObjectStore(directory.resolve("target"));
        StorageMigration migration = new StorageMigration(root, target);
        List<StorageMigration.BookFiles> books =
                List.of(new StorageMigration.BookFiles("a", "txt"));

        assertThat(migration.migrate(books).copied()).isEqualTo(2);
        assertThat(migration.migrate(books).skipped()).isEqualTo(2);
        assertThat(source).exists();
        assertThat(target.get("books/a/assets/img-1.png")).isPresent();
    }

    @Test
    void reportsMissingSource() {
        StorageMigration migration = new StorageMigration(
                directory, new LocalObjectStore(directory.resolve("target")));

        StorageMigration.Result result =
                migration.migrate(List.of(new StorageMigration.BookFiles("gone", "pdf")));

        assertThat(result.succeeded()).isFalse();
    }
}
