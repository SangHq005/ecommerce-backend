CREATE TABLE IF NOT EXISTS product_option_group (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  KEY idx_og_product (product_id),
  UNIQUE KEY uk_og_name (product_id, name),
  CONSTRAINT fk_og_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_option_value (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  option_group_id BIGINT NOT NULL,
  value VARCHAR(64) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  KEY idx_ov_group (option_group_id),
  UNIQUE KEY uk_ov_value (option_group_id, value),
  CONSTRAINT fk_ov_group FOREIGN KEY (option_group_id) REFERENCES product_option_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
