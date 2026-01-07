CREATE TABLE IF NOT EXISTS idempotency_key (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  idem_key VARCHAR(128) NOT NULL,
  scope VARCHAR(64) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  response_code INT NULL,
  response_body JSON NULL,
  status VARCHAR(32) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY uk_idem_scope_key (scope, idem_key),
  INDEX idx_idem_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
