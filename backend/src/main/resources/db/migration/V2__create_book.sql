CREATE TABLE books (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id      BIGINT       NOT NULL,
    title         VARCHAR(255) NOT NULL,
    author        VARCHAR(255),
    format        VARCHAR(8)   NOT NULL,
    is_private    BOOLEAN      NOT NULL,
    storage_key   CHAR(36)     NOT NULL,
    file_size     BIGINT       NOT NULL,
    created_at    TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_books_storage_key UNIQUE (storage_key),
    CONSTRAINT fk_books_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE INDEX idx_books_visibility ON books (is_private, created_at);
