package com.reader.reading;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reading_progress")
public class ReadingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    // 章節或頁碼
    @Column(nullable = false)
    private int location;

    @Column(nullable = false)
    private double ratio;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ReadingProgress() {
    }

    public ReadingProgress(Long userId, Long bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }

    public void moveTo(int location, double ratio) {
        this.location = location;
        this.ratio = ratio;
        this.updatedAt = Instant.now();
    }

    public int getLocation() {
        return location;
    }

    public double getRatio() {
        return ratio;
    }
}
