CREATE TABLE admins (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    permission    TINYINT      NOT NULL,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_admins_username UNIQUE (username),
    CONSTRAINT ck_admins_permission CHECK (permission IN (1, 2))
);
