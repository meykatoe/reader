package com.reader.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

// 兩種實作共用
abstract class ObjectStoreContract {

    @TempDir
    protected Path directory;

    protected abstract ObjectStore store();

    @Test
    void storesReadsAndDeletes() throws IOException {
        ObjectStore store = store();
        String prefix = "books/" + UUID.randomUUID() + "/";
        byte[] data = new byte[300_000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i * 31);
        }
        store.put(prefix + "source.bin", Files.write(directory.resolve("source"), data));
        store.put(prefix + "assets/a.png", new byte[] {1, 2, 3});

        Resource source = store.get(prefix + "source.bin").orElseThrow();
        assertThat(source.contentLength()).isEqualTo(data.length);
        try (InputStream input = source.getInputStream()) {
            assertThat(input.readAllBytes()).isEqualTo(data);
        }
        // 模擬分段下載
        try (InputStream input = source.getInputStream()) {
            input.skipNBytes(200_000);
            assertThat(input.readNBytes(100)).containsExactly(
                    Arrays.copyOfRange(data, 200_000, 200_100));
        }

        store.deleteDirectory(prefix);
        assertThat(store.get(prefix + "source.bin")).isEmpty();
        assertThat(store.get(prefix + "assets/a.png")).isEmpty();
    }
}
