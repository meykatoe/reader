package com.reader.book;

import com.reader.common.ApiException;
import java.util.Arrays;
import java.util.Locale;

public enum BookFormat {
    EPUB, TXT, PDF, MOBI;

    public String extension() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static BookFormat fromFilename(String filename) {
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(format -> lower.endsWith("." + format.extension()))
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("僅支援 EPUB、TXT、PDF、MOBI 格式"));
    }
}
