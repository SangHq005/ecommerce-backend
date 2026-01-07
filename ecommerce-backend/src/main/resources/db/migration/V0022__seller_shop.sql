CREATE TABLE IF NOT EXISTS seller_shop (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  seller_user_id BIGINT NOT NULL,
  shop_name VARCHAR(191) NOT NULL,
  shop_slug VARCHAR(191) NOT NULL,
  description LONGTEXT NULL,
  logo_url VARCHAR(512) NULL,
  banner_url VARCHAR(512) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- DRAFT/PENDING_REVIEW/ACTIVE/SUSPENDED
  verified_at DATETIME NULL,
  suspended_reason VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version BIGINT NOT NULL DEFAULT 0,

  UNIQUE KEY uk_shop_seller (seller_user_id),
  UNIQUE KEY uk_shop_slug (shop_slug),
  KEY idx_shop_status (status),
  CONSTRAINT fk_shop_seller FOREIGN KEY (seller_user_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
