package com.reader.storage;

import java.nio.file.Path;
import java.util.Optional;
import org.springframework.core.io.Resource;

// 物件儲存介面
public interface ObjectStore {

    void put(String key, Path source);

    void put(String key, byte[] data);

    Optional<Resource> get(String key);

    // 刪除整個目錄
    void deleteDirectory(String prefix);
}
