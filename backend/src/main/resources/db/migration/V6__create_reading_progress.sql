CREATE TABLE reading_progress (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    book_id    BIGINT       NOT NULL,
    location   INT          NOT NULL,
    ratio      DOUBLE       NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_progress_user_book UNIQUE (user_id, book_id),
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);
