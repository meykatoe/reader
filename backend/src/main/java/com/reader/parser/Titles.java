package com.reader.parser;

import org.jsoup.nodes.Element;

public final class Titles {

    private static final int MAX_LENGTH = 100;
    private static final int MAX_LINE_LENGTH = 30;

    private Titles() {
    }

    // 取首個標題
    public static String of(Element body, String fallback) {
        Element heading = body.selectFirst("h1, h2, h3");
        if (heading != null && heading.hasText()) {
            return limit(heading.text().strip());
        }
        // 首段短行
        return body.select("p").stream()
                .map(paragraph -> paragraph.text().strip())
                .filter(text -> !text.isEmpty())
                .findFirst()
                .filter(text -> text.length() <= MAX_LINE_LENGTH)
                .orElse(fallback);
    }

    private static String limit(String text) {
        return text.length() > MAX_LENGTH ? text.substring(0, MAX_LENGTH) : text;
    }
}
