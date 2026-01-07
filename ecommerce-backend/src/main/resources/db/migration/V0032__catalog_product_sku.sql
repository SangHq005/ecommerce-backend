CREATE TABLE IF NOT EXISTS product_sku (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,

  sku_code VARCHAR(64) NOT NULL,
  price BIGINT NOT NULL,
  stock_on_hand INT NOT NULL DEFAULT 0,
  reserved_stock INT NOT NULL DEFAULT 0,
  
  option_signature VARCHAR(255) NOT NULL DEFAULT '',
  option_signature_hash VARCHAR(64) NOT NULL DEFAULT '',
  compare_at_price BIGINT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  version BIGINT NOT NULL DEFAULT 0,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  KEY idx_sku_product (product_id),
  UNIQUE KEY uk_sku_code (product_id, sku_code),
  UNIQUE KEY uk_sku_sig (product_id, option_signature_hash),

  CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
