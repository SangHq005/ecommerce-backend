CREATE TABLE IF NOT EXISTS user_profile (
  user_id BIGINT PRIMARY KEY,
  full_name VARCHAR(128) NULL,
  phone VARCHAR(32) NULL,
  avatar_url VARCHAR(255) NULL,
  CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
