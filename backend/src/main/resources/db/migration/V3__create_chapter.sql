CREATE TABLE chapters (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    book_id       BIGINT       NOT NULL,
    chapter_index INT          NOT NULL,
    title         VARCHAR(255) NOT NULL,
    content       MEDIUMTEXT   NOT NULL,
    char_count    INT          NOT NULL,
    CONSTRAINT uk_chapters_book_index UNIQUE (book_id, chapter_index),
    CONSTRAINT fk_chapters_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);
