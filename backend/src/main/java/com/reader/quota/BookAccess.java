package com.reader.quota;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// 刪書仍計次
@Entity
@Table(name = "book_access")
public class BookAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 8)
    private AccessKind kind;

    @Column(name = "access_date", nullable = false)
    private LocalDate accessDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected BookAccess() {
    }

    public BookAccess(Long userId, Long bookId, AccessKind kind, LocalDate accessDate, Instant createdAt) {
        this.userId = userId;
        this.bookId = bookId;
        this.kind = kind;
        this.accessDate = accessDate;
        this.createdAt = createdAt;
    }
}
