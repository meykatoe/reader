package com.reader.book;

import com.reader.common.HardDeleteGuard;
import com.reader.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@EntityListeners(HardDeleteGuard.class)
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 空值為平臺
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // 舊版遺留
    @Column(name = "owner_deleted", nullable = false)
    private boolean ownerDeleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "deletion_cause", length = 8)
    private DeletionCause deletionCause;

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

    @Column(name = "page_count")
    private Integer pageCount;

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
        return owner != null && owner.getId().equals(userId);
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

    public void assignPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public String getCoverName() {
        return coverName;
    }

    public void rename(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String ownerName() {
        if (owner == null) {
            return ownerDeleted ? "已刪除用戶" : "平臺";
        }
        return owner.isDeleted() ? "已刪除用戶" : owner.getUsername();
    }

    public void softDelete(DeletionCause cause, Instant when) {
        this.deletedAt = when;
        this.deletionCause = cause;
    }

    public void restore() {
        this.deletedAt = null;
        this.deletionCause = null;
    }

    public Instant getDeletedAt() {
        return deletedAt;
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
