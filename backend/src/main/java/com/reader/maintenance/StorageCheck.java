package com.reader.maintenance;

import com.reader.storage.ObjectStore;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import org.springframework.core.io.Resource;

// 相容性檢查
class StorageCheck {

    private static final int SIZE = 20 * 1024 * 1024;
    private static final int RANGE_START = SIZE - 5000;

    private final ObjectStore store;

    StorageCheck(ObjectStore store) {
        this.store = store;
    }

    private interface Step {

        void run() throws IOException;
    }

    boolean run() throws IOException {
        String prefix = "reader-check/" + UUID.randomUUID() + "/";
        String large = prefix + "large.bin";
        String small = prefix + "assets/small.bin";
        byte[] data = new byte[SIZE];
        new Random().nextBytes(data);
        Path file = Files.createTempFile("storage-check", ".bin");
        try {
            Files.write(file, data);
            return step("上傳大型檔案", () -> store.put(large, file))
                    && step("上傳小型檔案", () -> store.put(small, new byte[] {1, 2, 3}))
                    && step("讀取檔案大小", () -> require(find(large).contentLength() == SIZE))
                    && step("完整下載", () -> require(Arrays.equals(read(large, 0, SIZE), data)))
                    && step("分段下載", () -> require(Arrays.equals(
                            read(large, RANGE_START, 1000),
                            Arrays.copyOfRange(data, RANGE_START, RANGE_START + 1000))))
                    && step("批次刪除", () -> store.deleteDirectory(prefix))
                    && step("確認已刪除", () -> require(
                            store.get(large).isEmpty() && store.get(small).isEmpty()));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private Resource find(String key) {
        return store.get(key).orElseThrow(() -> new IllegalStateException("找不到物件"));
    }

    private byte[] read(String key, int from, int length) throws IOException {
        try (InputStream input = find(key).getInputStream()) {
            input.skipNBytes(from);
            return input.readNBytes(length);
        }
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new IllegalStateException("結果不符預期");
        }
    }

    private static boolean step(String name, Step action) {
        try {
            action.run();
            System.out.println("OK   " + name);
            return true;
        } catch (IOException | RuntimeException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            System.out.println("FAIL " + name + "：" + cause.getMessage());
            return false;
        }
    }
}
