package com.reader.admin.book;

import com.reader.book.Book;
import com.reader.book.BookFormat;
import com.reader.user.User;
import java.time.Instant;

public record AdminBookResponse(
        Long id,
        String title,
        String author,
        BookFormat format,
        boolean isPrivate,
        Long ownerId,
        String ownerName,
        long fileSize,
        Instant createdAt,
        Instant deletedAt) {

    static AdminBookResponse of(Book book) {
        return new AdminBookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getFormat(),
                book.isPrivateBook(),
                book.getOwner() == null ? null : book.getOwner().getId(),
                ownerLabel(book),
                book.getFileSize(),
                book.getCreatedAt(),
                book.getDeletedAt());
    }

    // 後臺看原名
    private static String ownerLabel(Book book) {
        User owner = book.getOwner();
        if (owner == null) {
            return book.ownerName();
        }
        return owner.isDeleted() ? owner.getUsername() + "（已刪除）" : owner.getUsername();
    }
}
