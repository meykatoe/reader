package com.reader.parser;

import java.util.List;
import java.util.Map;

public record ParsedBook(
        String title,
        String author,
        List<ParsedChapter> chapters,
        Map<String, byte[]> assets) {

    public static ParsedBook textOnly(List<ParsedChapter> chapters) {
        return new ParsedBook(null, null, chapters, Map.of());
    }
}
