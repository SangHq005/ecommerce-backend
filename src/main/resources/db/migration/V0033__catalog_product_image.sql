CREATE TABLE IF NOT EXISTS product_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  image_url VARCHAR(512) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  KEY idx_image_product (product_id),
  CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES product(id),
  UNIQUE KEY uk_product_image_order (product_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
