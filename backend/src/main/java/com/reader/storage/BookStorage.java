package com.reader.storage;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

// 書籍檔案配置
@Component
public class BookStorage {

    private final ObjectStore store;

    public BookStorage(ObjectStore store) {
        this.store = store;
    }

    public void save(String bookKey, String extension, Path source, Map<String, byte[]> assets) {
        store.put(sourceKey(bookKey, extension), source);
        assets.forEach((name, data) -> store.put(assetKey(bookKey, name), data));
    }

    public Optional<Resource> source(String bookKey, String extension) {
        return store.get(sourceKey(bookKey, extension));
    }

    // 名稱須先驗證
    public Optional<Resource> asset(String bookKey, String name) {
        return store.get(assetKey(bookKey, name));
    }

    public void delete(String bookKey) {
        store.deleteDirectory(directory(bookKey));
    }

    public static String directory(String bookKey) {
        return "books/" + bookKey + "/";
    }

    private static String sourceKey(String bookKey, String extension) {
        return directory(bookKey) + "source." + extension;
    }

    private static String assetKey(String bookKey, String name) {
        return directory(bookKey) + "assets/" + name;
    }
}
