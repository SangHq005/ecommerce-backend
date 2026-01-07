-- Enhanced Sample Test Data for E-commerce Platform
-- This migration adds more comprehensive test data for development and testing

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================================
-- ADDITIONAL USERS (Customers, Sellers, Admin)
-- ============================================================================

-- More Customers
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'customer3@demo.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyễn Văn An', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='customer3@demo.local');

INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'customer4@demo.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Thị Bình', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='customer4@demo.local');

INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'customer5@demo.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Hoàng Cường', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='customer5@demo.local');

INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'customer6@demo.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Thị Dung', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='customer6@demo.local');

-- More Sellers
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'seller3@demo.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Võ Minh Tuấn', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='seller3@demo.local');

INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'seller4@demo.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Đặng Thị Hương', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='seller4@demo.local');

-- Assign roles to new users
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.code = 'CLIENT'
WHERE u.email IN ('customer3@demo.local', 'customer4@demo.local', 'customer5@demo.local', 'customer6@demo.local')
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.code = 'SELLER'
WHERE u.email IN ('seller3@demo.local', 'seller4@demo.local')
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

-- ============================================================================
-- USER PROFILES
-- ============================================================================

INSERT INTO user_profile (user_id, phone, gender, date_of_birth, avatar_url, updated_at)
SELECT u.id, '0901234567', 'MALE', '1990-05-15', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=128&h=128&q=80', NOW()
FROM app_user u
WHERE u.email = 'customer3@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_profile p WHERE p.user_id = u.id);

INSERT INTO user_profile (user_id, phone, gender, date_of_birth, avatar_url, updated_at)
SELECT u.id, '0902345678', 'FEMALE', '1995-08-20', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=128&h=128&q=80', NOW()
FROM app_user u
WHERE u.email = 'customer4@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_profile p WHERE p.user_id = u.id);

INSERT INTO user_profile (user_id, phone, gender, date_of_birth, avatar_url, updated_at)
SELECT u.id, '0903456789', 'MALE', '1988-12-10', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=128&h=128&q=80', NOW()
FROM app_user u
WHERE u.email = 'customer5@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_profile p WHERE p.user_id = u.id);

INSERT INTO user_profile (user_id, phone, gender, date_of_birth, avatar_url, updated_at)
SELECT u.id, '0904567890', 'FEMALE', '1992-03-25', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=128&h=128&q=80', NOW()
FROM app_user u
WHERE u.email = 'customer6@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_profile p WHERE p.user_id = u.id);

INSERT INTO user_profile (user_id, phone, gender, date_of_birth, avatar_url, updated_at)
SELECT u.id, '0905678901', 'MALE', '1985-07-18', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=128&h=128&q=80', NOW()
FROM app_user u
WHERE u.email = 'seller3@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_profile p WHERE p.user_id = u.id);

INSERT INTO user_profile (user_id, phone, gender, date_of_birth, avatar_url, updated_at)
SELECT u.id, '0906789012', 'FEMALE', '1993-11-05', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=128&h=128&q=80', NOW()
FROM app_user u
WHERE u.email = 'seller4@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_profile p WHERE p.user_id = u.id);

-- ============================================================================
-- USER ADDRESSES
-- ============================================================================

INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Nguyễn Văn An', '0901234567', '789 Lê Lợi', 'Phường Bến Nghé', 'Quận 1', 'TP. Hồ Chí Minh', TRUE
FROM app_user u
WHERE u.email = 'customer3@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_address a WHERE a.user_id = u.id);

INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Trần Thị Bình', '0902345678', '456 Nguyễn Huệ', 'Phường Bến Thành', 'Quận 1', 'TP. Hồ Chí Minh', TRUE
FROM app_user u
WHERE u.email = 'customer4@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_address a WHERE a.user_id = u.id);

INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Lê Hoàng Cường', '0903456789', '123 Trần Hưng Đạo', 'Phường Cầu Ông Lãnh', 'Quận 1', 'TP. Hồ Chí Minh', TRUE
FROM app_user u
WHERE u.email = 'customer5@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_address a WHERE a.user_id = u.id);

INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Phạm Thị Dung', '0904567890', '321 Võ Văn Tần', 'Phường 5', 'Quận 3', 'TP. Hồ Chí Minh', TRUE
FROM app_user u
WHERE u.email = 'customer6@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_address a WHERE a.user_id = u.id);

-- Secondary addresses for some users
INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Nguyễn Văn An (Văn phòng)', '0901234567', '100 Pasteur', 'Phường Bến Nghé', 'Quận 1', 'TP. Hồ Chí Minh', FALSE
FROM app_user u
WHERE u.email = 'customer3@demo.local'
  AND NOT EXISTS (SELECT 1 FROM user_address a WHERE a.user_id = u.id AND a.is_default = FALSE);

-- ============================================================================
-- SELLER SHOPS
-- ============================================================================

INSERT INTO seller_shop (seller_user_id, shop_name, shop_slug, description, logo_url, banner_url, status, verified_at)
SELECT u.id, 'TechWorld Vietnam', 'techworld-vietnam', 'Chuyên cung cấp thiết bị công nghệ chính hãng', 
  'https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=200&h=200&q=80',
  'https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=1200&h=400&q=80',
  'ACTIVE', NOW()
FROM app_user u
WHERE u.email = 'seller3@demo.local'
  AND NOT EXISTS (SELECT 1 FROM seller_shop s WHERE s.seller_user_id = u.id);

INSERT INTO seller_shop (seller_user_id, shop_name, shop_slug, description, logo_url, banner_url, status, verified_at)
SELECT u.id, 'Fashion House', 'fashion-house', 'Thời trang cao cấp cho mọi lứa tuổi', 
  'https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=200&h=200&q=80',
  'https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?auto=format&fit=crop&w=1200&h=400&q=80',
  'ACTIVE', NOW()
FROM app_user u
WHERE u.email = 'seller4@demo.local'
  AND NOT EXISTS (SELECT 1 FROM seller_shop s WHERE s.seller_user_id = u.id);

-- ============================================================================
-- CATEGORIES
-- ============================================================================

-- More Electronics subcategories
INSERT INTO category (parent_id, name, slug, path, is_active, sort_order, created_at, updated_at)
SELECT c.id, 'Tablets', 'tablets', CONCAT(c.path, '/tablets'), TRUE, 3, NOW(), NOW()
FROM category c
WHERE c.name = 'Electronics'
  AND NOT EXISTS (SELECT 1 FROM category WHERE name = 'Tablets');

INSERT INTO category (parent_id, name, slug, path, is_active, sort_order, created_at, updated_at)
SELECT c.id, 'Headphones', 'headphones', CONCAT(c.path, '/headphones'), TRUE, 4, NOW(), NOW()
FROM category c
WHERE c.name = 'Electronics'
  AND NOT EXISTS (SELECT 1 FROM category WHERE name = 'Headphones');

INSERT INTO category (parent_id, name, slug, path, is_active, sort_order, created_at, updated_at)
SELECT c.id, 'Smartwatches', 'smartwatches', CONCAT(c.path, '/smartwatches'), TRUE, 5, NOW(), NOW()
FROM category c
WHERE c.name = 'Electronics'
  AND NOT EXISTS (SELECT 1 FROM category WHERE name = 'Smartwatches');

-- Fashion subcategories
INSERT INTO category (parent_id, name, slug, path, is_active, sort_order, created_at, updated_at)
SELECT c.id, 'Men Clothing', 'men-clothing', CONCAT(c.path, '/men-clothing'), TRUE, 1, NOW(), NOW()
FROM category c
WHERE c.name = 'Fashion'
  AND NOT EXISTS (SELECT 1 FROM category WHERE name = 'Men Clothing');

INSERT INTO category (parent_id, name, slug, path, is_active, sort_order, created_at, updated_at)
SELECT c.id, 'Women Clothing', 'women-clothing', CONCAT(c.path, '/women-clothing'), TRUE, 2, NOW(), NOW()
FROM category c
WHERE c.name = 'Fashion'
  AND NOT EXISTS (SELECT 1 FROM category WHERE name = 'Women Clothing');

INSERT INTO category (parent_id, name, slug, path, is_active, sort_order, created_at, updated_at)
SELECT c.id, 'Shoes', 'shoes', CONCAT(c.path, '/shoes'), TRUE, 3, NOW(), NOW()
FROM category c
WHERE c.name = 'Fashion'
  AND NOT EXISTS (SELECT 1 FROM category WHERE name = 'Shoes');

-- New top-level categories
INSERT INTO category (parent_id, name, slug, path, is_active, sort_order, created_at, updated_at)
SELECT NULL, 'Home & Living', 'home-living', '/home-living', TRUE, 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Home & Living');

INSERT INTO category (parent_id, name, slug, path, is_active, sort_order, created_at, updated_at)
SELECT NULL, 'Sports & Outdoors', 'sports-outdoors', '/sports-outdoors', TRUE, 4, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Sports & Outdoors');

INSERT INTO category (parent_id, name, slug, path, is_active, sort_order, created_at, updated_at)
SELECT NULL, 'Books & Media', 'books-media', '/books-media', TRUE, 5, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Books & Media');

-- ============================================================================
-- BRANDS
-- ============================================================================

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Sony', 'sony', 'https://images.unsplash.com/photo-1611532736597-de2d4265fba3?auto=format&fit=crop&w=200&h=200&q=80', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = 'Sony');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Dell', 'dell', 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=200&h=200&q=80', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = 'Dell');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Adidas', 'adidas', 'https://images.unsplash.com/photo-1556906781-9a412961c28c?auto=format&fit=crop&w=200&h=200&q=80', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = 'Adidas');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Zara', 'zara', 'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=200&h=200&q=80', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = 'Zara');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Canon', 'canon', 'https://images.unsplash.com/photo-1606800052052-a08af7148866?auto=format&fit=crop&w=200&h=200&q=80', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = 'Canon');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Xiaomi', 'xiaomi', 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=200&h=200&q=80', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = 'Xiaomi');

-- ============================================================================
-- PRODUCTS
-- ============================================================================

-- Apple Store Products
INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'iPad Pro 12.9 M2', 'ipad-pro-12-9-m2', 
  'Máy tính bảng cao cấp với chip M2, màn hình Liquid Retina XDR 12.9 inch, hỗ trợ Apple Pencil thế hệ 2',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=800&q=80',
  27990000, 25, 'IPAD-PRO-M2-128', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official'
  AND c.name = 'Tablets'
  AND b.name = 'Apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'iPad Pro 12.9 M2' AND p.shop_id = s.id);

INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'AirPods Pro 2nd Gen', 'airpods-pro-2nd-gen', 
  'Tai nghe không dây với chống ồn chủ động, âm thanh không gian, hộp sạc MagSafe',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?auto=format&fit=crop&w=800&q=80',
  6990000, 100, 'AIRPODS-PRO-2', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official'
  AND c.name = 'Headphones'
  AND b.name = 'Apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'AirPods Pro 2nd Gen' AND p.shop_id = s.id);

INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'Apple Watch Series 9', 'apple-watch-series-9', 
  'Đồng hồ thông minh với màn hình Always-On Retina, cảm biến sức khỏe tiên tiến, GPS',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d?auto=format&fit=crop&w=800&q=80',
  11990000, 60, 'WATCH-S9-41MM', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official'
  AND c.name = 'Smartwatches'
  AND b.name = 'Apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Apple Watch Series 9' AND p.shop_id = s.id);

-- TechWorld Products
INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'Dell XPS 15', 'dell-xps-15', 
  'Laptop cao cấp Intel Core i7, RAM 16GB, SSD 512GB, màn hình 15.6 inch 4K OLED',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=800&q=80',
  45990000, 15, 'DELL-XPS15-I7', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'TechWorld Vietnam'
  AND c.name = 'Laptops'
  AND b.name = 'Dell'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Dell XPS 15' AND p.shop_id = s.id);

INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'Sony WH-1000XM5', 'sony-wh-1000xm5', 
  'Tai nghe chống ồn cao cấp, âm thanh Hi-Res, pin 30 giờ, kết nối đa điểm',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=800&q=80',
  8990000, 45, 'SONY-WH1000XM5', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'TechWorld Vietnam'
  AND c.name = 'Headphones'
  AND b.name = 'Sony'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Sony WH-1000XM5' AND p.shop_id = s.id);

INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'Xiaomi Redmi Note 13 Pro', 'xiaomi-redmi-note-13-pro', 
  'Smartphone tầm trung camera 200MP, màn hình AMOLED 120Hz, sạc nhanh 67W',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=800&q=80',
  7990000, 80, 'XIAOMI-RN13P-8GB', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'TechWorld Vietnam'
  AND c.name = 'Phones'
  AND b.name = 'Xiaomi'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Xiaomi Redmi Note 13 Pro' AND p.shop_id = s.id);

-- Fashion House Products
INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'Nike Air Max 270', 'nike-air-max-270', 
  'Giày thể thao nam với đệm khí Max Air lớn nhất, thiết kế hiện đại, thoải mái',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80',
  3990000, 120, 'NIKE-AM270-42', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Fashion House'
  AND c.name = 'Shoes'
  AND b.name = 'Nike'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Nike Air Max 270' AND p.shop_id = s.id);

INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'Adidas Ultraboost 23', 'adidas-ultraboost-23', 
  'Giày chạy bộ cao cấp với công nghệ Boost, đế Continental, upper Primeknit',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1608231387042-66d1773070a5?auto=format&fit=crop&w=800&q=80',
  4990000, 90, 'ADIDAS-UB23-42', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Fashion House'
  AND c.name = 'Shoes'
  AND b.name = 'Adidas'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Adidas Ultraboost 23' AND p.shop_id = s.id);

INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'Zara Men Slim Fit Suit', 'zara-men-slim-fit-suit', 
  'Bộ vest nam công sở, form slim fit hiện đại, chất liệu cao cấp',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80',
  2990000, 50, 'ZARA-SUIT-M-L', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Fashion House'
  AND c.name = 'Men Clothing'
  AND b.name = 'Zara'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Zara Men Slim Fit Suit' AND p.shop_id = s.id);

INSERT INTO product (shop_id, seller_user_id, category_id, brand_id, name, slug, description, status, main_image_url, price, stock_quantity, sku, currency, created_at, updated_at)
SELECT s.id, s.seller_user_id, c.id, b.id, 
  'Zara Women Floral Dress', 'zara-women-floral-dress', 
  'Váy hoa nữ dáng xòe, chất liệu voan mềm mại, phù hợp dự tiệc',
  'ACTIVE', 
  'https://images.unsplash.com/photo-1595777457583-95e059d581b8?auto=format&fit=crop&w=800&q=80',
  1590000, 75, 'ZARA-DRESS-W-M', 'VND', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Fashion House'
  AND c.name = 'Women Clothing'
  AND b.name = 'Zara'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Zara Women Floral Dress' AND p.shop_id = s.id);

-- ============================================================================
-- PRODUCT IMAGES
-- ============================================================================

-- iPad Pro images
INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=800&q=80', 1
FROM product p
WHERE p.name = 'iPad Pro 12.9 M2'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 1);

INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1561154464-82e9adf32764?auto=format&fit=crop&w=800&q=80', 2
FROM product p
WHERE p.name = 'iPad Pro 12.9 M2'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 2);

-- AirPods Pro images
INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?auto=format&fit=crop&w=800&q=80', 1
FROM product p
WHERE p.name = 'AirPods Pro 2nd Gen'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 1);

-- Dell XPS images
INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=800&q=80', 1
FROM product p
WHERE p.name = 'Dell XPS 15'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 1);

INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=800&q=80', 2
FROM product p
WHERE p.name = 'Dell XPS 15'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 2);

-- ============================================================================
-- PRODUCT SKUs
-- ============================================================================

-- iPad Pro SKUs
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'IPAD-PRO-M2-128', 27990000, 25, 0, 'default', MD5('IPAD-PRO-M2-128'), NOW(), NOW()
FROM product p
WHERE p.name = 'iPad Pro 12.9 M2'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'IPAD-PRO-M2-128');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'IPAD-PRO-M2-256', 32990000, 20, 0, 'default', MD5('IPAD-PRO-M2-256'), NOW(), NOW()
FROM product p
WHERE p.name = 'iPad Pro 12.9 M2'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'IPAD-PRO-M2-256');

-- AirPods Pro SKU
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'AIRPODS-PRO-2', 6990000, 100, 0, 'default', MD5('AIRPODS-PRO-2'), NOW(), NOW()
FROM product p
WHERE p.name = 'AirPods Pro 2nd Gen'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'AIRPODS-PRO-2');

-- Apple Watch SKUs
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'WATCH-S9-41MM', 11990000, 60, 0, 'default', MD5('WATCH-S9-41MM'), NOW(), NOW()
FROM product p
WHERE p.name = 'Apple Watch Series 9'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'WATCH-S9-41MM');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'WATCH-S9-45MM', 13990000, 40, 0, 'default', MD5('WATCH-S9-45MM'), NOW(), NOW()
FROM product p
WHERE p.name = 'Apple Watch Series 9'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'WATCH-S9-45MM');

-- Dell XPS SKU
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'DELL-XPS15-I7', 45990000, 15, 0, 'default', MD5('DELL-XPS15-I7'), NOW(), NOW()
FROM product p
WHERE p.name = 'Dell XPS 15'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'DELL-XPS15-I7');

-- Sony Headphones SKU
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'SONY-WH1000XM5', 8990000, 45, 0, 'default', MD5('SONY-WH1000XM5'), NOW(), NOW()
FROM product p
WHERE p.name = 'Sony WH-1000XM5'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'SONY-WH1000XM5');

-- Xiaomi Phone SKUs
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'XIAOMI-RN13P-8GB', 7990000, 80, 0, 'default', MD5('XIAOMI-RN13P-8GB'), NOW(), NOW()
FROM product p
WHERE p.name = 'Xiaomi Redmi Note 13 Pro'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'XIAOMI-RN13P-8GB');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'XIAOMI-RN13P-12GB', 9990000, 60, 0, 'default', MD5('XIAOMI-RN13P-12GB'), NOW(), NOW()
FROM product p
WHERE p.name = 'Xiaomi Redmi Note 13 Pro'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'XIAOMI-RN13P-12GB');

-- Nike Shoes SKUs (different sizes)
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'NIKE-AM270-40', 3990000, 30, 0, 'default', MD5('NIKE-AM270-40'), NOW(), NOW()
FROM product p
WHERE p.name = 'Nike Air Max 270'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'NIKE-AM270-40');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'NIKE-AM270-42', 3990000, 40, 0, 'default', MD5('NIKE-AM270-42'), NOW(), NOW()
FROM product p
WHERE p.name = 'Nike Air Max 270'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'NIKE-AM270-42');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'NIKE-AM270-44', 3990000, 50, 0, 'default', MD5('NIKE-AM270-44'), NOW(), NOW()
FROM product p
WHERE p.name = 'Nike Air Max 270'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'NIKE-AM270-44');

-- Adidas SKU
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'ADIDAS-UB23-42', 4990000, 90, 0, 'default', MD5('ADIDAS-UB23-42'), NOW(), NOW()
FROM product p
WHERE p.name = 'Adidas Ultraboost 23'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'ADIDAS-UB23-42');

-- Zara SKUs
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'ZARA-SUIT-M-L', 2990000, 50, 0, 'default', MD5('ZARA-SUIT-M-L'), NOW(), NOW()
FROM product p
WHERE p.name = 'Zara Men Slim Fit Suit'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'ZARA-SUIT-M-L');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'ZARA-DRESS-W-M', 1590000, 75, 0, 'default', MD5('ZARA-DRESS-W-M'), NOW(), NOW()
FROM product p
WHERE p.name = 'Zara Women Floral Dress'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'ZARA-DRESS-W-M');

-- ============================================================================
-- ORDERS
-- ============================================================================

INSERT INTO orders (order_code, user_id, shop_id, status, total_amount, currency, created_at, updated_at)
SELECT 'ORD-2026-0003', u.id, s.id, 'PENDING', 6990000, 'VND', NOW(), NOW()
FROM app_user u, seller_shop s
WHERE u.email = 'customer3@demo.local'
  AND s.shop_name = 'Apple Store Official'
  AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_code = 'ORD-2026-0003');

INSERT INTO orders (order_code, user_id, shop_id, status, total_amount, currency, created_at, updated_at)
SELECT 'ORD-2026-0004', u.id, s.id, 'DELIVERED', 8990000, 'VND', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()
FROM app_user u, seller_shop s
WHERE u.email = 'customer4@demo.local'
  AND s.shop_name = 'TechWorld Vietnam'
  AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_code = 'ORD-2026-0004');

INSERT INTO orders (order_code, user_id, shop_id, status, total_amount, currency, created_at, updated_at)
SELECT 'ORD-2026-0005', u.id, s.id, 'PAID', 3990000, 'VND', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()
FROM app_user u, seller_shop s
WHERE u.email = 'customer5@demo.local'
  AND s.shop_name = 'Fashion House'
  AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_code = 'ORD-2026-0005');

INSERT INTO orders (order_code, user_id, shop_id, status, total_amount, currency, created_at, updated_at)
SELECT 'ORD-2026-0006', u.id, s.id, 'CANCELLED', 45990000, 'VND', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()
FROM app_user u, seller_shop s
WHERE u.email = 'customer6@demo.local'
  AND s.shop_name = 'TechWorld Vietnam'
  AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_code = 'ORD-2026-0006');

-- ============================================================================
-- ORDER ITEMS
-- ============================================================================

INSERT INTO order_item (order_id, product_id, sku_id, quantity, unit_price, total_price)
SELECT o.id, p.id, s.id, 1, 6990000, 6990000
FROM orders o
JOIN product p ON p.name = 'AirPods Pro 2nd Gen'
JOIN product_sku s ON s.sku_code = 'AIRPODS-PRO-2'
WHERE o.order_code = 'ORD-2026-0003'
  AND NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.id AND i.sku_id = s.id);

INSERT INTO order_item (order_id, product_id, sku_id, quantity, unit_price, total_price)
SELECT o.id, p.id, s.id, 1, 8990000, 8990000
FROM orders o
JOIN product p ON p.name = 'Sony WH-1000XM5'
JOIN product_sku s ON s.sku_code = 'SONY-WH1000XM5'
WHERE o.order_code = 'ORD-2026-0004'
  AND NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.id AND i.sku_id = s.id);

INSERT INTO order_item (order_id, product_id, sku_id, quantity, unit_price, total_price)
SELECT o.id, p.id, s.id, 1, 3990000, 3990000
FROM orders o
JOIN product p ON p.name = 'Nike Air Max 270'
JOIN product_sku s ON s.sku_code = 'NIKE-AM270-42'
WHERE o.order_code = 'ORD-2026-0005'
  AND NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.id AND i.sku_id = s.id);

INSERT INTO order_item (order_id, product_id, sku_id, quantity, unit_price, total_price)
SELECT o.id, p.id, s.id, 1, 45990000, 45990000
FROM orders o
JOIN product p ON p.name = 'Dell XPS 15'
JOIN product_sku s ON s.sku_code = 'DELL-XPS15-I7'
WHERE o.order_code = 'ORD-2026-0006'
  AND NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.id AND i.sku_id = s.id);

-- ============================================================================
-- CART ITEMS
-- ============================================================================

INSERT INTO cart_item (user_id, shop_id, product_id, sku_id, quantity, created_at, updated_at)
SELECT u.id, sh.id, p.id, s.id, 1, NOW(), NOW()
FROM app_user u
JOIN seller_shop sh ON sh.shop_name = 'TechWorld Vietnam'
JOIN product p ON p.name = 'Xiaomi Redmi Note 13 Pro'
JOIN product_sku s ON s.sku_code = 'XIAOMI-RN13P-8GB'
WHERE u.email = 'customer3@demo.local'
  AND NOT EXISTS (SELECT 1 FROM cart_item c WHERE c.user_id = u.id AND c.sku_id = s.id);

INSERT INTO cart_item (user_id, shop_id, product_id, sku_id, quantity, created_at, updated_at)
SELECT u.id, sh.id, p.id, s.id, 2, NOW(), NOW()
FROM app_user u
JOIN seller_shop sh ON sh.shop_name = 'Fashion House'
JOIN product p ON p.name = 'Zara Women Floral Dress'
JOIN product_sku s ON s.sku_code = 'ZARA-DRESS-W-M'
WHERE u.email = 'customer4@demo.local'
  AND NOT EXISTS (SELECT 1 FROM cart_item c WHERE c.user_id = u.id AND c.sku_id = s.id);

INSERT INTO cart_item (user_id, shop_id, product_id, sku_id, quantity, created_at, updated_at)
SELECT u.id, sh.id, p.id, s.id, 1, NOW(), NOW()
FROM app_user u
JOIN seller_shop sh ON sh.shop_name = 'Apple Store Official'
JOIN product p ON p.name = 'Apple Watch Series 9'
JOIN product_sku s ON s.sku_code = 'WATCH-S9-41MM'
WHERE u.email = 'customer5@demo.local'
  AND NOT EXISTS (SELECT 1 FROM cart_item c WHERE c.user_id = u.id AND c.sku_id = s.id);

-- ============================================================================
-- PAYMENTS
-- ============================================================================

INSERT INTO payments (order_id, amount, currency, method, status, transaction_id, gateway, gateway_response, created_at, updated_at)
SELECT o.id, 8990000, 'VND', 'VNPAY', 'COMPLETED', 'VNPAY-0002', 'VNPAY', '{"mock":true,"status":"success"}', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()
FROM orders o
WHERE o.order_code = 'ORD-2026-0004'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.order_id = o.id);

INSERT INTO payments (order_id, amount, currency, method, status, transaction_id, gateway, gateway_response, created_at, updated_at)
SELECT o.id, 3990000, 'VND', 'COD', 'COMPLETED', 'COD-0001', 'COD', '{"method":"cash_on_delivery"}', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()
FROM orders o
WHERE o.order_code = 'ORD-2026-0005'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.order_id = o.id);

-- ============================================================================
-- REVIEWS
-- ============================================================================

INSERT INTO review (product_id, user_id, order_id, rating, comment, images, status, created_at)
SELECT p.id, u.id, o.id, 5, 
  'Tai nghe chống ồn tuyệt vời! Âm thanh rất hay, pin trâu, đáng đồng tiền bát gạo.', 
  '["https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=800&q=80"]', 
  'APPROVED', DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM product p, app_user u, orders o
WHERE p.name = 'Sony WH-1000XM5'
  AND u.email = 'customer4@demo.local'
  AND o.order_code = 'ORD-2026-0004'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id);

INSERT INTO review (product_id, user_id, order_id, rating, comment, images, status, created_at)
SELECT p.id, u.id, o.id, 4, 
  'Giày đẹp, êm chân, nhưng size hơi to một chút. Nên chọn size nhỏ hơn bình thường.', 
  '["https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80"]', 
  'APPROVED', DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM product p, app_user u, orders o
WHERE p.name = 'Nike Air Max 270'
  AND u.email = 'customer5@demo.local'
  AND o.order_code = 'ORD-2026-0005'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id);

INSERT INTO review (product_id, user_id, order_id, rating, comment, images, status, created_at)
SELECT p.id, u.id, o.id, 5, 
  'Điện thoại chất lượng tốt trong tầm giá. Camera đẹp, pin khỏe, sạc nhanh. Rất hài lòng!', 
  NULL, 
  'APPROVED', DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM product p, app_user u, orders o
WHERE p.name = 'Galaxy S24'
  AND u.email = 'client2@demo.local'
  AND o.order_code = 'ORD-2026-0002'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id);

-- ============================================================================
-- WISHLIST ITEMS
-- ============================================================================

INSERT INTO wishlist_item (user_id, product_id, added_at, note)
SELECT u.id, p.id, NOW(), 'Muốn mua khi có khuyến mãi'
FROM app_user u, product p
WHERE u.email = 'customer3@demo.local'
  AND p.name = 'iPad Pro 12.9 M2'
  AND NOT EXISTS (SELECT 1 FROM wishlist_item w WHERE w.user_id = u.id AND w.product_id = p.id);

INSERT INTO wishlist_item (user_id, product_id, added_at, note)
SELECT u.id, p.id, NOW(), 'Đợi sale'
FROM app_user u, product p
WHERE u.email = 'customer4@demo.local'
  AND p.name = 'Dell XPS 15'
  AND NOT EXISTS (SELECT 1 FROM wishlist_item w WHERE w.user_id = u.id AND w.product_id = p.id);

INSERT INTO wishlist_item (user_id, product_id, added_at, note)
SELECT u.id, p.id, NOW(), 'Quà sinh nhật'
FROM app_user u, product p
WHERE u.email = 'customer5@demo.local'
  AND p.name = 'Adidas Ultraboost 23'
  AND NOT EXISTS (SELECT 1 FROM wishlist_item w WHERE w.user_id = u.id AND w.product_id = p.id);

INSERT INTO wishlist_item (user_id, product_id, added_at, note)
SELECT u.id, p.id, NOW(), NULL
FROM app_user u, product p
WHERE u.email = 'customer6@demo.local'
  AND p.name = 'Apple Watch Series 9'
  AND NOT EXISTS (SELECT 1 FROM wishlist_item w WHERE w.user_id = u.id AND w.product_id = p.id);

-- ============================================================================
-- COUPONS
-- ============================================================================

INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount, min_order_amount,
  start_date, end_date, usage_limit, usage_count, usage_limit_per_user, auto_apply,
  applicable_product_ids, applicable_category_ids, applicable_user_ids, created_at, updated_at)
SELECT 'TECH2026', 'Tech Sale 2026', 'Giảm 15% cho sản phẩm công nghệ', 'PERCENTAGE', 'ACTIVE', 15, 2000000, 5000000,
  DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY), 200, 0, 2, FALSE,
  NULL, NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'TECH2026');

INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount, min_order_amount,
  start_date, end_date, usage_limit, usage_count, usage_limit_per_user, auto_apply,
  applicable_product_ids, applicable_category_ids, applicable_user_ids, created_at, updated_at)
SELECT 'FASHION20', 'Fashion Week', 'Giảm 20% cho thời trang', 'PERCENTAGE', 'ACTIVE', 20, 500000, 1000000,
  NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 150, 0, 3, FALSE,
  NULL, NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'FASHION20');

INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount, min_order_amount,
  start_date, end_date, usage_limit, usage_count, usage_limit_per_user, auto_apply,
  applicable_product_ids, applicable_category_ids, applicable_user_ids, created_at, updated_at)
SELECT 'FREESHIP', 'Miễn phí vận chuyển', 'Giảm 50K phí ship', 'FIXED_AMOUNT', 'ACTIVE', 50000, NULL, 200000,
  DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 500, 0, 5, FALSE,
  NULL, NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'FREESHIP');

INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount, min_order_amount,
  start_date, end_date, usage_limit, usage_count, usage_limit_per_user, auto_apply,
  applicable_product_ids, applicable_category_ids, applicable_user_ids, created_at, updated_at)
SELECT 'MEGA1M', 'Mega Sale 1 Triệu', 'Giảm 1 triệu cho đơn hàng lớn', 'FIXED_AMOUNT', 'ACTIVE', 1000000, NULL, 10000000,
  NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 20, 0, 1, FALSE,
  NULL, NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'MEGA1M');

-- ============================================================================
-- STOCK MOVEMENTS
-- ============================================================================

INSERT INTO stock_movement (sku_id, delta, reason, actor_id, idem_scope, idem_key, created_at)
SELECT s.id, 100, 'INITIAL_STOCK', u.id, 'seed-enhanced', 'stock-airpods-pro-2', NOW()
FROM product_sku s, app_user u
WHERE s.sku_code = 'AIRPODS-PRO-2'
  AND u.email = 'seller1@demo.local'
  AND NOT EXISTS (SELECT 1 FROM stock_movement m WHERE m.idem_scope = 'seed-enhanced' AND m.idem_key = 'stock-airpods-pro-2');

INSERT INTO stock_movement (sku_id, delta, reason, actor_id, idem_scope, idem_key, created_at)
SELECT s.id, 45, 'INITIAL_STOCK', u.id, 'seed-enhanced', 'stock-sony-wh1000xm5', NOW()
FROM product_sku s, app_user u
WHERE s.sku_code = 'SONY-WH1000XM5'
  AND u.email = 'seller3@demo.local'
  AND NOT EXISTS (SELECT 1 FROM stock_movement m WHERE m.idem_scope = 'seed-enhanced' AND m.idem_key = 'stock-sony-wh1000xm5');

INSERT INTO stock_movement (sku_id, delta, reason, actor_id, idem_scope, idem_key, created_at)
SELECT s.id, 120, 'INITIAL_STOCK', u.id, 'seed-enhanced', 'stock-nike-am270', NOW()
FROM product_sku s, app_user u
WHERE s.sku_code = 'NIKE-AM270-42'
  AND u.email = 'seller4@demo.local'
  AND NOT EXISTS (SELECT 1 FROM stock_movement m WHERE m.idem_scope = 'seed-enhanced' AND m.idem_key = 'stock-nike-am270');

-- ============================================================================
-- STOCK RESERVATIONS
-- ============================================================================

INSERT INTO stock_reservation (order_token, sku_id, qty, status, created_at, updated_at, expires_at)
SELECT 'TOKEN-ORD-2026-0003', s.id, 1, 'RESERVED', NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE)
FROM product_sku s
WHERE s.sku_code = 'AIRPODS-PRO-2'
  AND NOT EXISTS (SELECT 1 FROM stock_reservation r WHERE r.order_token = 'TOKEN-ORD-2026-0003' AND r.sku_id = s.id);

INSERT INTO stock_reservation (order_token, sku_id, qty, status, created_at, updated_at, expires_at)
SELECT 'TOKEN-ORD-2026-0004', s.id, 1, 'COMMITTED', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE)
FROM product_sku s
WHERE s.sku_code = 'SONY-WH1000XM5'
  AND NOT EXISTS (SELECT 1 FROM stock_reservation r WHERE r.order_token = 'TOKEN-ORD-2026-0004' AND r.sku_id = s.id);

SET FOREIGN_KEY_CHECKS=1;
