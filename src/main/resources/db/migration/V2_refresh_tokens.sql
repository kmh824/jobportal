CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id            BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              user_id       BIGINT      NOT NULL,
                                              token_hash    CHAR(64)    NOT NULL UNIQUE, -- SHA-256 hex
    issued_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at    DATETIME    NOT NULL,
    revoked_at    DATETIME         NULL,
    replaced_by_id BIGINT          NULL,
    user_agent    VARCHAR(255)     NULL,
    ip            VARCHAR(45)      NULL,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_rt_user (user_id),
    INDEX idx_rt_expires (expires_at)
    );
