CREATE TABLE user_api_logs (
    id             BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    request_id     VARCHAR(16)   NOT NULL,
    account_id     BIGINT,
    account        VARCHAR(64),
    method         VARCHAR(8)    NOT NULL,
    path           VARCHAR(255)  NOT NULL,
    http_status    INT           NOT NULL,
    result_code    INT           NOT NULL,
    result_message VARCHAR(255)  NOT NULL,
    error_detail   VARCHAR(1000),
    ip             VARCHAR(45)   NOT NULL,
    duration_ms    BIGINT        NOT NULL,
    created_at     DATETIME(6)   NOT NULL
);

CREATE INDEX idx_user_log_time ON user_api_logs (created_at);
CREATE INDEX idx_user_log_account ON user_api_logs (account, created_at);
CREATE INDEX idx_user_log_code ON user_api_logs (result_code, created_at);
CREATE INDEX idx_user_log_request ON user_api_logs (request_id);

CREATE TABLE admin_api_logs (
    id             BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    request_id     VARCHAR(16)   NOT NULL,
    account_id     BIGINT,
    account        VARCHAR(64),
    method         VARCHAR(8)    NOT NULL,
    path           VARCHAR(255)  NOT NULL,
    http_status    INT           NOT NULL,
    result_code    INT           NOT NULL,
    result_message VARCHAR(255)  NOT NULL,
    error_detail   VARCHAR(1000),
    ip             VARCHAR(45)   NOT NULL,
    duration_ms    BIGINT        NOT NULL,
    created_at     DATETIME(6)   NOT NULL
);

CREATE INDEX idx_admin_log_time ON admin_api_logs (created_at);
CREATE INDEX idx_admin_log_account ON admin_api_logs (account, created_at);
CREATE INDEX idx_admin_log_code ON admin_api_logs (result_code, created_at);
CREATE INDEX idx_admin_log_request ON admin_api_logs (request_id);
