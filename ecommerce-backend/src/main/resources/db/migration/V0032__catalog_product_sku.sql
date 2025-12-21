CREATE TABLE IF NOT EXISTS product_sku (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,

  sku_code VARCHAR(64) NULL,
  price BIGINT NOT NULL,
  stock_on_hand INT NOT NULL DEFAULT 0,
  reserved_stock INT NOT NULL DEFAULT 0,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

  KEY idx_sku_product (product_id),
  UNIQUE KEY uk_sku_code (sku_code),

  CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
