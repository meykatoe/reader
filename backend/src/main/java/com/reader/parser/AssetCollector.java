package com.reader.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

// 圖片收集
public class AssetCollector {

    private static final long MAX_TOTAL_BYTES = 200L * 1024 * 1024;

    private final Map<String, byte[]> assets = new LinkedHashMap<>();
    private final Map<String, String> namesBySource = new HashMap<>();
    private long totalBytes;

    public Optional<String> add(String sourceKey, Supplier<byte[]> loader) {
        String existing = namesBySource.get(sourceKey);
        if (existing != null) {
            return Optional.of(existing);
        }
        byte[] data = loader.get();
        if (data == null) {
            return Optional.empty();
        }
        return ImageType.detect(data).map(type -> store(sourceKey, data, type));
    }

    public Map<String, byte[]> assets() {
        return Collections.unmodifiableMap(assets);
    }

    private String store(String sourceKey, byte[] data, ImageType type) {
        totalBytes += data.length;
        if (totalBytes > MAX_TOTAL_BYTES) {
            throw new ParseException("書籍內的圖片總量過大");
        }
        String name = "img-" + (assets.size() + 1) + "." + type.extension();
        assets.put(name, data);
        namesBySource.put(sourceKey, name);
        return name;
    }
}
