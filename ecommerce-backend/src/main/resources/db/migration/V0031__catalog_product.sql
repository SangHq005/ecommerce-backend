CREATE TABLE IF NOT EXISTS product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  brand_id BIGINT NULL,

  name VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL,
  description LONGTEXT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- DRAFT/PENDING/ACTIVE/REJECTED
  
  main_image_url VARCHAR(512) NULL,
  price BIGINT NOT NULL DEFAULT 0,
  stock_quantity INT NOT NULL DEFAULT 0,
  sku VARCHAR(100) NULL,
  currency VARCHAR(8) NOT NULL DEFAULT 'VND',
  
  seller_user_id BIGINT NOT NULL DEFAULT 0, -- Added to match Entity

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

  version BIGINT NOT NULL DEFAULT 0, -- Added version for optimistic locking

  KEY idx_product_shop (shop_id),
  KEY idx_product_status (status),
  KEY idx_product_category (category_id),
  KEY idx_product_brand (brand_id),
  UNIQUE KEY uk_product_shop_slug (shop_id, slug),

  CONSTRAINT fk_product_shop FOREIGN KEY (shop_id) REFERENCES seller_shop(id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
