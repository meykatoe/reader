package com.reader.parser;

import org.jsoup.nodes.Element;

public final class Titles {

    private static final int MAX_LENGTH = 100;

    private Titles() {
    }

    // 取首個標題
    public static String of(Element body, String fallback) {
        Element heading = body.selectFirst("h1, h2, h3");
        String text = heading == null ? "" : heading.text().strip();
        if (text.isEmpty()) {
            return fallback;
        }
        return text.length() > MAX_LENGTH ? text.substring(0, MAX_LENGTH) : text;
    }
}
