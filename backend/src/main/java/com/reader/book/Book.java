package com.reader.book;

import com.reader.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String title;

    private String author;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 8)
    private BookFormat format;

    @Column(name = "is_private", nullable = false)
    private boolean privateBook;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "storage_key", nullable = false, length = 36)
    private String storageKey;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "cover_name", length = 64)
    private String coverName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Book() {
    }

    public Book(User owner, String title, String author, BookFormat format,
                boolean privateBook, String storageKey, long fileSize) {
        this.owner = owner;
        this.title = title;
        this.author = author;
        this.format = format;
        this.privateBook = privateBook;
        this.storageKey = storageKey;
        this.fileSize = fileSize;
        this.createdAt = Instant.now();
    }

    public boolean isOwnedBy(Long userId) {
        return owner.getId().equals(userId);
    }

    public boolean isVisibleTo(Long userId) {
        return !privateBook || isOwnedBy(userId);
    }

    public void changeVisibility(boolean privateBook) {
        this.privateBook = privateBook;
    }

    public void assignCover(String coverName) {
        this.coverName = coverName;
    }

    public String getCoverName() {
        return coverName;
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BookFormat getFormat() {
        return format;
    }

    public boolean isPrivateBook() {
        return privateBook;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
