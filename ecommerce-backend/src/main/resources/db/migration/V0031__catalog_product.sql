CREATE TABLE IF NOT EXISTS product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  category_id BIGINT NULL,
  brand_id BIGINT NULL,

  name VARCHAR(255) NOT NULL,
  description TEXT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- DRAFT/PENDING/ACTIVE/REJECTED
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

  KEY idx_product_shop (shop_id),
  KEY idx_product_status (status),
  KEY idx_product_category (category_id),
  KEY idx_product_brand (brand_id),

  CONSTRAINT fk_product_shop FOREIGN KEY (shop_id) REFERENCES seller_shop(id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
