package com.reader.book.dto;

import com.reader.book.Book;
import com.reader.book.BookFormat;
import java.time.Instant;

public record BookResponse(
        Long id,
        String title,
        String author,
        BookFormat format,
        boolean isPrivate,
        boolean mine,
        String ownerName,
        long fileSize,
        boolean hasCover,
        Integer pageCount,
        Instant createdAt) {

    public static BookResponse of(Book book, Long viewerId) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getFormat(),
                book.isPrivateBook(),
                book.isOwnedBy(viewerId),
                book.getOwner().getUsername(),
                book.getFileSize(),
                book.getCoverName() != null,
                book.getPageCount(),
                book.getCreatedAt());
    }
}
