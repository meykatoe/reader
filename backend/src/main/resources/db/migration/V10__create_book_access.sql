CREATE TABLE book_access (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    book_id     BIGINT      NOT NULL,
    kind        VARCHAR(8)  NOT NULL,
    access_date DATE        NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    CONSTRAINT uk_access_daily UNIQUE (user_id, kind, access_date, book_id),
    CONSTRAINT fk_access_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
