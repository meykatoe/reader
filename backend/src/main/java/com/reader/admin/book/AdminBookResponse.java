package com.reader.admin.book;

import com.reader.book.Book;
import com.reader.book.BookFormat;
import java.time.Instant;

public record AdminBookResponse(
        Long id,
        String title,
        String author,
        BookFormat format,
        boolean isPrivate,
        Long ownerId,
        String ownerName,
        boolean ownerDeleted,
        long fileSize,
        Instant createdAt) {

    static AdminBookResponse of(Book book) {
        return new AdminBookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getFormat(),
                book.isPrivateBook(),
                book.getOwner() == null ? null : book.getOwner().getId(),
                book.ownerName(),
                book.isOwnerDeleted(),
                book.getFileSize(),
                book.getCreatedAt());
    }
}
