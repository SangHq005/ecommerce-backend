CREATE TABLE IF NOT EXISTS refresh_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_root_jti VARCHAR(64) NOT NULL,
    refresh_jti VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    user_agent VARCHAR(255) NULL,
    ip VARCHAR(64) NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    rotated_at DATETIME(6) NULL,
    revoked_at DATETIME(6) NULL,

    UNIQUE KEY uk_refresh_jti (refresh_jti),
    KEY idx_session_user (user_id, status, expires_at),
    KEY idx_session_root (session_root_jti, status),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
