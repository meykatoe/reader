package com.reader.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "chapters")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "chapter_index", nullable = false)
    private int index;

    @Column(nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false, length = 16_777_215)
    private String content;

    @Column(name = "char_count", nullable = false)
    private int charCount;

    protected Chapter() {
    }

    public Chapter(Long bookId, int index, String title, String content, int charCount) {
        this.bookId = bookId;
        this.index = index;
        this.title = title;
        this.content = content;
        this.charCount = charCount;
    }

    public int getIndex() {
        return index;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getCharCount() {
        return charCount;
    }
}
