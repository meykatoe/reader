package com.reader.quota;

import com.reader.book.Book;

final class DownloadFilename {

    private static final int MAX_LENGTH = 100;

    private DownloadFilename() {
    }

    // 清除非法字
    static String of(Book book) {
        String name = book.getTitle().replaceAll("[\\p{Cntrl}\\\\/:*?\"<>|]", "_").strip();
        if (name.length() > MAX_LENGTH) {
            name = name.substring(0, MAX_LENGTH);
        }
        return (name.isEmpty() ? "book" : name) + "." + book.getFormat().extension();
    }
}
