-- =============================================================================
-- V0118: BỔ SUNG SẢN PHẨM VÀ ATTRIBUTES ĐỂ ĐỦ 100 SẢN PHẨM (10 SẢN PHẨM/CATEGORY)
-- =============================================================================
-- Mục tiêu: Mỗi category có đúng 10 sản phẩm với đầy đủ attributes
-- =============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- =============================================================================
-- 1. THÊM BRANDS CÒN THIẾU
-- =============================================================================

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Garmin', 'garmin', 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Garmin_logo.svg/200px-Garmin_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'garmin');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Huawei', 'huawei', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Huawei_logo.svg/200px-Huawei_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'huawei');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Fitbit', 'fitbit', 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/9a/Fitbit_logo.svg/200px-Fitbit_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'fitbit');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Amazfit', 'amazfit', 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Amazfit_logo.svg/200px-Amazfit_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'amazfit');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'TicWatch', 'ticwatch', 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/TicWatch_logo.svg/200px-TicWatch_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'ticwatch');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Fossil', 'fossil', 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Fossil_logo.svg/200px-Fossil_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'fossil');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'OnePlus', 'oneplus', 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c9/OnePlus_logo.svg/200px-OnePlus_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'oneplus');

-- =============================================================================
-- 2. BỔ SUNG SẢN PHẨM ĐIỆN THOẠI (ĐỦ 10 SẢN PHẨM)
-- =============================================================================

-- Vivo X100 Pro
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Vivo X100 Pro 512GB - Camera Zeiss', 'vivo-x100-pro-512gb-zeiss',
  '<h2>Vivo X100 Pro - Camera Zeiss chuyên nghiệp</h2><p>Vivo X100 Pro với camera Zeiss, chip MediaTek Dimensity 9300 và sạc siêu nhanh 100W.</p>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070379/e2c901fe7a59bce2a479706c3cc553d6_uqkttd.jpg',
  26990000, 28990000, 70, 'VND', 'ACTIVE', 4.6, 98, 420, 225, FALSE, 87, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'vivo'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'vivo-x100-pro-512gb-zeiss' AND p.shop_id = s.id);

-- Realme GT 5 Pro
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Realme GT 5 Pro 256GB - Flagship giá tốt', 'realme-gt-5-pro-256gb',
  '<h2>Realme GT 5 Pro - Flagship giá tốt</h2><p>Realme GT 5 Pro với Snapdragon 8 Gen 3, camera 50MP và sạc nhanh 100W.</p>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070440/d6b24d652da0179d9116b0bb7b8c8cad_ws8xgn.jpg',
  14990000, 16990000, 120, 'VND', 'ACTIVE', 4.5, 145, 890, 218, FALSE, 85, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'realme'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'realme-gt-5-pro-256gb' AND p.shop_id = s.id);

-- OnePlus 12
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'OnePlus 12 256GB - Flagship OxygenOS', 'oneplus-12-256gb',
  '<h2>OnePlus 12 - Flagship với OxygenOS</h2><p>OnePlus 12 với Snapdragon 8 Gen 3, camera Hasselblad và sạc nhanh 100W.</p>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070484/555fd1b76d39017fbb3f667490a88818_wpl5sb.jpg',
  19990000, 21990000, 90, 'VND', 'ACTIVE', 4.7, 167, 720, 220, TRUE, 89, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'oneplus'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'oneplus-12-256gb' AND p.shop_id = s.id);

-- =============================================================================
-- 3. BỔ SUNG SẢN PHẨM LAPTOP (ĐỦ 10 SẢN PHẨM)
-- =============================================================================

-- HP Pavilion Plus 14
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'HP Pavilion Plus 14 - Core i5 Gen 13', 'hp-pavilion-plus-14-i5',
  '<h2>HP Pavilion Plus 14 - Laptop mỏng nhẹ</h2><p>Pavilion Plus 14 với Core i5 thế hệ 13, màn hình OLED và thiết kế mỏng nhẹ.</p>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070526/02b630210da124defa695e80660f4a66_ilvp2x.jpg',
  19990000, 21990000, 60, 'VND', 'ACTIVE', 4.5, 89, 450, 1400, FALSE, 85, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'laptop' AND b.slug = 'hp'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'hp-pavilion-plus-14-i5' AND p.shop_id = s.id);

-- Acer Predator Helios 16
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Acer Predator Helios 16 - RTX 4060', 'acer-predator-helios-16-rtx4060',
  '<h2>Acer Predator Helios 16 - Gaming laptop</h2><p>Predator Helios 16 với Core i7, RTX 4060 và màn hình 165Hz.</p>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070575/3eb7f47ad57f7a2b13c76db8f135ee5d_e1oovx.jpg',
  34990000, 37990000, 35, 'VND', 'ACTIVE', 4.6, 78, 320, 2600, FALSE, 88, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'laptop' AND b.slug = 'acer'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'acer-predator-helios-16-rtx4060' AND p.shop_id = s.id);

-- MSI Katana 15
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'MSI Katana 15 - RTX 4050', 'msi-katana-15-rtx4050',
  '<h2>MSI Katana 15 - Gaming giá tốt</h2><p>Katana 15 với Core i5, RTX 4050 và màn hình 144Hz.</p>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070633/9d895d1d087c4facbe743502e1215481_sfqloy.jpg',
  22990000, 24990000, 45, 'VND', 'ACTIVE', 4.4, 112, 580, 2100, FALSE, 83, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'laptop' AND b.slug = 'msi'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'msi-katana-15-rtx4050' AND p.shop_id = s.id);

-- ASUS Vivobook 15
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'ASUS Vivobook 15 - Ryzen 5', 'asus-vivobook-15-ryzen5',
  '<h2>ASUS Vivobook 15 - Laptop văn phòng</h2><p>Vivobook 15 với Ryzen 5, màn hình FHD và thiết kế mỏng nhẹ.</p>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070671/94de4818decdaa616de77c9a7462f0ee_ifhucq.jpg',
  12990000, 14990000, 80, 'VND', 'ACTIVE', 4.3, 156, 1200, 1700, FALSE, 80, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'laptop' AND b.slug = 'asus'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'asus-vivobook-15-ryzen5' AND p.shop_id = s.id);

-- Lenovo IdeaPad 3
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Lenovo IdeaPad 3 - Core i3', 'lenovo-ideapad-3-i3',
  '<h2>Lenovo IdeaPad 3 - Laptop học tập</h2><p>IdeaPad 3 với Core i3, RAM 8GB và SSD 256GB phù hợp học tập.</p>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070714/06f13f3d14f00ec4c6cb4d50e5a8e072_dkd6b6.jpg',
  8990000, 10990000, 100, 'VND', 'ACTIVE', 4.2, 234, 2100, 1800, FALSE, 78, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'laptop' AND b.slug = 'lenovo'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'lenovo-ideapad-3-i3' AND p.shop_id = s.id);

SET FOREIGN_KEY_CHECKS=1;
