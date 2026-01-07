CREATE TABLE IF NOT EXISTS review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comment LONGTEXT NULL,
  images JSON NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  helpful_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY uk_review_product_user (product_id, user_id),
  KEY idx_review_product (product_id),
  KEY idx_review_user (user_id),
  KEY idx_review_status (status),

  CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS review_helpful (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY uk_review_user (review_id, user_id),
  CONSTRAINT fk_helpful_review FOREIGN KEY (review_id) REFERENCES review(id) ON DELETE CASCADE,
  CONSTRAINT fk_helpful_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
