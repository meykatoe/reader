package com.reader.parser;

import java.util.Arrays;
import java.util.Optional;

// 僅收點陣圖
public enum ImageType {
    JPG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    GIF("gif", "image/gif"),
    WEBP("webp", "image/webp");

    private final String extension;
    private final String mediaType;

    ImageType(String extension, String mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String extension() {
        return extension;
    }

    public String mediaType() {
        return mediaType;
    }

    public static Optional<ImageType> detect(byte[] data) {
        if (matches(data, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of(JPG);
        }
        if (matches(data, 0, 0x89, 'P', 'N', 'G')) {
            return Optional.of(PNG);
        }
        if (matches(data, 0, 'G', 'I', 'F', '8')) {
            return Optional.of(GIF);
        }
        if (matches(data, 0, 'R', 'I', 'F', 'F') && matches(data, 8, 'W', 'E', 'B', 'P')) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    public static Optional<ImageType> fromName(String name) {
        return Arrays.stream(values())
                .filter(type -> name.endsWith("." + type.extension))
                .findFirst();
    }

    private static boolean matches(byte[] data, int offset, int... signature) {
        if (data.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((data[offset + i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }
}
