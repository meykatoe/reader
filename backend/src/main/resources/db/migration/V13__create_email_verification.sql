CREATE TABLE email_verifications (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(254) NOT NULL,
    purpose    VARCHAR(16)  NOT NULL,
    code_hash  VARCHAR(100) NOT NULL,
    token_hash VARCHAR(64)  NOT NULL,
    attempts   INT          NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    CONSTRAINT uk_verification_target UNIQUE (email, purpose),
    CONSTRAINT uk_verification_token UNIQUE (token_hash)
);

CREATE INDEX idx_verification_expiry ON email_verifications (expires_at);
