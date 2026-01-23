-- =============================================================================
-- E-Commerce Backend - Complete Seed Data Script
-- =============================================================================
-- This script creates a complete set of test data matching the current schema.
-- 
-- Usage:
--   docker exec -i ecommerce-mysql mysql -u ecommerce -p<password> ecommerce < scripts/seed-data.sql
--
-- Test credentials (password: Password123!):
--   admin@demo.local    - Administrator
--   seller1@demo.local  - Apple Store seller
--   seller2@demo.local  - Samsung Hub seller  
--   client1@demo.local  - Test client
--   client2@demo.local  - Test client
--
-- BCrypt hash for 'Password123!' = $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
-- =============================================================================

SET FOREIGN_KEY_CHECKS=0;
SET @bcrypt_password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG';

-- =============================================================================
-- 1. ROLES
-- =============================================================================
INSERT IGNORE INTO role (code, name) VALUES 
('ADMIN', 'Administrator'),
('SELLER', 'Seller'),
('CLIENT', 'Client');

-- =============================================================================
-- 2. USERS
-- =============================================================================
-- Admin user
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'admin@demo.local', @bcrypt_password, 'System Admin', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'admin@demo.local');

-- Sellers
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'seller1@demo.local', @bcrypt_password, 'Nguyen Van Apple', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'seller1@demo.local');

INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'seller2@demo.local', @bcrypt_password, 'Tran Thi Samsung', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'seller2@demo.local');

-- Clients
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'client1@demo.local', @bcrypt_password, 'Le Van Khach', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'client1@demo.local');

INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'client2@demo.local', @bcrypt_password, 'Pham Thi Mua', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email = 'client2@demo.local');

-- =============================================================================
-- 3. USER ROLES
-- =============================================================================
-- Admin gets ADMIN role
INSERT IGNORE INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u, role r 
WHERE u.email = 'admin@demo.local' AND r.code = 'ADMIN';

-- Sellers get SELLER + CLIENT roles  
INSERT IGNORE INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u, role r 
WHERE u.email IN ('seller1@demo.local', 'seller2@demo.local') AND r.code = 'SELLER';

INSERT IGNORE INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u, role r 
WHERE u.email IN ('seller1@demo.local', 'seller2@demo.local') AND r.code = 'CLIENT';

-- Clients get CLIENT role
INSERT IGNORE INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u, role r 
WHERE u.email IN ('client1@demo.local', 'client2@demo.local') AND r.code = 'CLIENT';

-- =============================================================================
-- 4. USER PROFILES
-- =============================================================================
INSERT INTO user_profile (user_id, phone, avatar_url, updated_at)
SELECT u.id, '0901000001', NULL, NOW()
FROM app_user u WHERE u.email = 'admin@demo.local'
AND NOT EXISTS (SELECT 1 FROM user_profile WHERE user_id = u.id);

INSERT INTO user_profile (user_id, phone, avatar_url, updated_at)
SELECT u.id, '0901000002', 'https://i.pravatar.cc/150?u=seller1', NOW()
FROM app_user u WHERE u.email = 'seller1@demo.local'
AND NOT EXISTS (SELECT 1 FROM user_profile WHERE user_id = u.id);

INSERT INTO user_profile (user_id, phone, avatar_url, updated_at)
SELECT u.id, '0901000003', 'https://i.pravatar.cc/150?u=seller2', NOW()
FROM app_user u WHERE u.email = 'seller2@demo.local'
AND NOT EXISTS (SELECT 1 FROM user_profile WHERE user_id = u.id);

INSERT INTO user_profile (user_id, phone, avatar_url, updated_at)
SELECT u.id, '0901000004', 'https://i.pravatar.cc/150?u=client1', NOW()
FROM app_user u WHERE u.email = 'client1@demo.local'
AND NOT EXISTS (SELECT 1 FROM user_profile WHERE user_id = u.id);

INSERT INTO user_profile (user_id, phone, avatar_url, updated_at)
SELECT u.id, '0901000005', 'https://i.pravatar.cc/150?u=client2', NOW()
FROM app_user u WHERE u.email = 'client2@demo.local'
AND NOT EXISTS (SELECT 1 FROM user_profile WHERE user_id = u.id);

-- =============================================================================
-- 5. USER ADDRESSES
-- =============================================================================
INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Le Van Khach', '0901000004', '123 Nguyen Hue, Quan 1', 'Phuong Ben Nghe', 'Quan 1', 'Ho Chi Minh', TRUE
FROM app_user u WHERE u.email = 'client1@demo.local'
AND NOT EXISTS (SELECT 1 FROM user_address WHERE user_id = u.id AND is_default = TRUE);

INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Le Van Khach - Van phong', '0901000004', '456 Le Loi, Quan 3', 'Phuong 7', 'Quan 3', 'Ho Chi Minh', FALSE
FROM app_user u WHERE u.email = 'client1@demo.local'
AND NOT EXISTS (SELECT 1 FROM user_address WHERE user_id = u.id AND is_default = FALSE);

INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Pham Thi Mua', '0901000005', '789 Tran Hung Dao, Quan 5', 'Phuong 1', 'Quan 5', 'Ho Chi Minh', TRUE
FROM app_user u WHERE u.email = 'client2@demo.local'
AND NOT EXISTS (SELECT 1 FROM user_address WHERE user_id = u.id AND is_default = TRUE);

-- =============================================================================
-- 6. SELLER SHOPS
-- =============================================================================
INSERT INTO seller_shop (seller_user_id, shop_name, shop_slug, description, status, created_at, updated_at)
SELECT u.id, 'Apple Store Vietnam', 'apple-store-vietnam', 
       'Cua hang chinh hang Apple tai Viet Nam. Cam ket 100% hang chinh hang, bao hanh 12 thang.',
       'ACTIVE', NOW(), NOW()
FROM app_user u WHERE u.email = 'seller1@demo.local'
AND NOT EXISTS (SELECT 1 FROM seller_shop WHERE seller_user_id = u.id);

INSERT INTO seller_shop (seller_user_id, shop_name, shop_slug, description, status, created_at, updated_at)
SELECT u.id, 'Samsung Official Store', 'samsung-official', 
       'Dai ly uy quyen Samsung. Chuyen dien thoai, may tinh bang, phu kien Samsung chinh hang.',
       'ACTIVE', NOW(), NOW()
FROM app_user u WHERE u.email = 'seller2@demo.local'
AND NOT EXISTS (SELECT 1 FROM seller_shop WHERE seller_user_id = u.id);

-- =============================================================================
-- 7. CATEGORIES
-- =============================================================================
-- Root categories
INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Dien tu', 'dien-tu', '/dien-tu', NULL, TRUE, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'dien-tu');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Thoi trang', 'thoi-trang', '/thoi-trang', NULL, TRUE, 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'thoi-trang');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Nha cua & Doi song', 'nha-cua-doi-song', '/nha-cua-doi-song', NULL, TRUE, 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'nha-cua-doi-song');

-- Sub-categories: Dien tu
INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Dien thoai', 'dien-thoai', '/dien-tu/dien-thoai', 
       (SELECT id FROM category WHERE slug = 'dien-tu'), TRUE, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'dien-thoai');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Laptop', 'laptop', '/dien-tu/laptop', 
       (SELECT id FROM category WHERE slug = 'dien-tu'), TRUE, 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'laptop');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'May tinh bang', 'may-tinh-bang', '/dien-tu/may-tinh-bang', 
       (SELECT id FROM category WHERE slug = 'dien-tu'), TRUE, 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'may-tinh-bang');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Phu kien', 'phu-kien', '/dien-tu/phu-kien', 
       (SELECT id FROM category WHERE slug = 'dien-tu'), TRUE, 4, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'phu-kien');

-- Sub-categories: Thoi trang
INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Giay dep', 'giay-dep', '/thoi-trang/giay-dep', 
       (SELECT id FROM category WHERE slug = 'thoi-trang'), TRUE, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'giay-dep');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Quan ao nam', 'quan-ao-nam', '/thoi-trang/quan-ao-nam', 
       (SELECT id FROM category WHERE slug = 'thoi-trang'), TRUE, 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'quan-ao-nam');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Quan ao nu', 'quan-ao-nu', '/thoi-trang/quan-ao-nu', 
       (SELECT id FROM category WHERE slug = 'thoi-trang'), TRUE, 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE slug = 'quan-ao-nu');

-- =============================================================================
-- 8. BRANDS
-- =============================================================================
INSERT IGNORE INTO brand (name, slug, is_active, created_at, updated_at) VALUES
('Apple', 'apple', TRUE, NOW(), NOW()),
('Samsung', 'samsung', TRUE, NOW(), NOW()),
('Xiaomi', 'xiaomi', TRUE, NOW(), NOW()),
('Sony', 'sony', TRUE, NOW(), NOW()),
('LG', 'lg', TRUE, NOW(), NOW()),
('Nike', 'nike', TRUE, NOW(), NOW()),
('Adidas', 'adidas', TRUE, NOW(), NOW()),
('Uniqlo', 'uniqlo', TRUE, NOW(), NOW());

-- =============================================================================
-- 9. PRODUCTS - Apple Store
-- =============================================================================
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, 
                     main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
       'iPhone 15 Pro Max', 'iphone-15-pro-max',
       'iPhone 15 Pro Max voi chip A17 Pro manh me nhat, camera 48MP, khung titanium cao cap. Man hinh Super Retina XDR 6.7 inch.',
       'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800',
       34990000, 100, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_slug = 'apple-store-vietnam' AND c.slug = 'dien-thoai' AND b.slug = 'apple'
AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'iphone-15-pro-max' AND shop_id = s.id);

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description,
                     main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
       'MacBook Pro 14 M3 Pro', 'macbook-pro-14-m3-pro',
       'MacBook Pro 14 inch voi chip M3 Pro, 18GB RAM, 512GB SSD. Hieu nang vuot troi cho cong viec chuyen nghiep.',
       'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800',
       52990000, 50, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_slug = 'apple-store-vietnam' AND c.slug = 'laptop' AND b.slug = 'apple'
AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'macbook-pro-14-m3-pro' AND shop_id = s.id);

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description,
                     main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
       'iPad Pro 12.9 M2', 'ipad-pro-12-m2',
       'iPad Pro 12.9 inch voi chip M2, man hinh Liquid Retina XDR, ho tro Apple Pencil 2.',
       'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800',
       31990000, 30, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_slug = 'apple-store-vietnam' AND c.slug = 'may-tinh-bang' AND b.slug = 'apple'
AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'ipad-pro-12-m2' AND shop_id = s.id);

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description,
                     main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
       'AirPods Pro 2 USB-C', 'airpods-pro-2-usbc',
       'AirPods Pro the he 2 voi cong USB-C, chong on chu dong, am thanh khong gian.',
       'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=800',
       6990000, 200, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_slug = 'apple-store-vietnam' AND c.slug = 'phu-kien' AND b.slug = 'apple'
AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'airpods-pro-2-usbc' AND shop_id = s.id);

-- =============================================================================
-- 10. PRODUCTS - Samsung Store
-- =============================================================================
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description,
                     main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
       'Samsung Galaxy S24 Ultra', 'samsung-galaxy-s24-ultra',
       'Galaxy S24 Ultra voi but S-Pen tich hop, camera 200MP, chip Snapdragon 8 Gen 3, man hinh Dynamic AMOLED 2X.',
       'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800',
       33990000, 80, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_slug = 'samsung-official' AND c.slug = 'dien-thoai' AND b.slug = 'samsung'
AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'samsung-galaxy-s24-ultra' AND shop_id = s.id);

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description,
                     main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
       'Samsung Galaxy Tab S9 Ultra', 'samsung-galaxy-tab-s9-ultra',
       'Galaxy Tab S9 Ultra voi man hinh Super AMOLED 14.6 inch, S-Pen, chip Snapdragon 8 Gen 2.',
       'https://images.unsplash.com/photo-1585790050230-5dd28404ccb9?w=800',
       28990000, 40, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_slug = 'samsung-official' AND c.slug = 'may-tinh-bang' AND b.slug = 'samsung'
AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'samsung-galaxy-tab-s9-ultra' AND shop_id = s.id);

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description,
                     main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
       'Samsung Galaxy Buds2 Pro', 'samsung-galaxy-buds2-pro',
       'Tai nghe Galaxy Buds2 Pro voi chong on chu dong, am thanh Hi-Fi 24bit, ket noi da thiet bi.',
       'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800',
       4990000, 150, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_slug = 'samsung-official' AND c.slug = 'phu-kien' AND b.slug = 'samsung'
AND NOT EXISTS (SELECT 1 FROM product WHERE slug = 'samsung-galaxy-buds2-pro' AND shop_id = s.id);

-- =============================================================================
-- 11. PRODUCT SKUS - Apple Products
-- =============================================================================
-- iPhone 15 Pro Max variants
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, 
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'IP15PM-256-NAT', 34990000, 30, 0, 
       'storage:256GB,color:Natural Titanium', MD5('storage:256GB,color:Natural Titanium'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'iphone-15-pro-max'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'IP15PM-256-NAT');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'IP15PM-256-BLK', 34990000, 25, 0,
       'storage:256GB,color:Black Titanium', MD5('storage:256GB,color:Black Titanium'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'iphone-15-pro-max'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'IP15PM-256-BLK');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'IP15PM-512-NAT', 41990000, 20, 0,
       'storage:512GB,color:Natural Titanium', MD5('storage:512GB,color:Natural Titanium'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'iphone-15-pro-max'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'IP15PM-512-NAT');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'IP15PM-1TB-BLK', 49990000, 15, 0,
       'storage:1TB,color:Black Titanium', MD5('storage:1TB,color:Black Titanium'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'iphone-15-pro-max'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'IP15PM-1TB-BLK');

-- MacBook Pro variants
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'MBP14-M3P-512-SG', 52990000, 20, 0,
       'chip:M3 Pro,storage:512GB,color:Space Gray', MD5('chip:M3 Pro,storage:512GB,color:Space Gray'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'macbook-pro-14-m3-pro'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'MBP14-M3P-512-SG');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'MBP14-M3P-1TB-SLV', 59990000, 15, 0,
       'chip:M3 Pro,storage:1TB,color:Silver', MD5('chip:M3 Pro,storage:1TB,color:Silver'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'macbook-pro-14-m3-pro'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'MBP14-M3P-1TB-SLV');

-- iPad Pro variants
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'IPADP12-256-SG', 31990000, 15, 0,
       'storage:256GB,color:Space Gray', MD5('storage:256GB,color:Space Gray'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'ipad-pro-12-m2'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'IPADP12-256-SG');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'IPADP12-512-SLV', 38990000, 10, 0,
       'storage:512GB,color:Silver', MD5('storage:512GB,color:Silver'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'ipad-pro-12-m2'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'IPADP12-512-SLV');

-- AirPods Pro variants
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'APP2-USBC', 6990000, 200, 0,
       'color:White', MD5('color:White'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'airpods-pro-2-usbc'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'APP2-USBC');

-- =============================================================================
-- 12. PRODUCT SKUS - Samsung Products
-- =============================================================================
-- Galaxy S24 Ultra variants
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'S24U-256-VIO', 33990000, 30, 0,
       'storage:256GB,color:Titanium Violet', MD5('storage:256GB,color:Titanium Violet'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'samsung-galaxy-s24-ultra'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'S24U-256-VIO');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'S24U-256-BLK', 33990000, 25, 0,
       'storage:256GB,color:Titanium Black', MD5('storage:256GB,color:Titanium Black'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'samsung-galaxy-s24-ultra'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'S24U-256-BLK');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'S24U-512-YEL', 39990000, 15, 0,
       'storage:512GB,color:Titanium Yellow', MD5('storage:512GB,color:Titanium Yellow'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'samsung-galaxy-s24-ultra'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'S24U-512-YEL');

-- Galaxy Tab S9 Ultra variants
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'TABS9U-256-GRY', 28990000, 20, 0,
       'storage:256GB,color:Graphite', MD5('storage:256GB,color:Graphite'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'samsung-galaxy-tab-s9-ultra'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'TABS9U-256-GRY');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'TABS9U-512-BEI', 34990000, 15, 0,
       'storage:512GB,color:Beige', MD5('storage:512GB,color:Beige'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'samsung-galaxy-tab-s9-ultra'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'TABS9U-512-BEI');

-- Galaxy Buds2 Pro variants
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'BUDS2P-GRY', 4990000, 80, 0,
       'color:Graphite', MD5('color:Graphite'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'samsung-galaxy-buds2-pro'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'BUDS2P-GRY');

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock,
                         option_signature, option_signature_hash, is_active, created_at, updated_at)
SELECT p.id, 'BUDS2P-WHT', 4990000, 70, 0,
       'color:White', MD5('color:White'), TRUE, NOW(), NOW()
FROM product p WHERE p.slug = 'samsung-galaxy-buds2-pro'
AND NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'BUDS2P-WHT');

-- =============================================================================
-- 13. PRODUCT IMAGES
-- =============================================================================
INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800', 1
FROM product p WHERE p.slug = 'iphone-15-pro-max'
AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 1);

INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800', 2
FROM product p WHERE p.slug = 'iphone-15-pro-max'
AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 2);

INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800', 1
FROM product p WHERE p.slug = 'macbook-pro-14-m3-pro'
AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 1);

INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800', 1
FROM product p WHERE p.slug = 'samsung-galaxy-s24-ultra'
AND NOT EXISTS (SELECT 1 FROM product_image WHERE product_id = p.id AND sort_order = 1);

-- =============================================================================
-- 14. COUPONS
-- =============================================================================
INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount, 
                    min_order_amount, start_date, end_date, usage_limit, usage_count, 
                    usage_limit_per_user, auto_apply, created_at, updated_at)
SELECT 'WELCOME10', 'Chao mung thanh vien moi', 'Giam 10% cho don hang dau tien (toi da 100.000d)',
       'PERCENTAGE', 'ACTIVE', 10, 100000, 200000,
       DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY),
       1000, 0, 1, FALSE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'WELCOME10');

INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount,
                    min_order_amount, start_date, end_date, usage_limit, usage_count,
                    usage_limit_per_user, auto_apply, created_at, updated_at)
SELECT 'SAVE500K', 'Giam 500.000d', 'Giam truc tiep 500.000d cho don hang tu 5.000.000d',
       'FIXED_AMOUNT', 'ACTIVE', 500000, NULL, 5000000,
       DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY),
       200, 0, 2, FALSE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'SAVE500K');

INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount,
                    min_order_amount, start_date, end_date, usage_limit, usage_count,
                    usage_limit_per_user, auto_apply, created_at, updated_at)
SELECT 'FREESHIP', 'Mien phi van chuyen', 'Mien phi ship cho tat ca don hang',
       'FIXED_AMOUNT', 'ACTIVE', 30000, NULL, 0,
       DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY),
       500, 0, 5, FALSE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'FREESHIP');

INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount,
                    min_order_amount, start_date, end_date, usage_limit, usage_count,
                    usage_limit_per_user, auto_apply, created_at, updated_at)
SELECT 'FLASH20', 'Flash Sale 20%', 'Giam 20% trong 24 gio (toi da 200.000d)',
       'PERCENTAGE', 'ACTIVE', 20, 200000, 500000,
       NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY),
       100, 0, 1, FALSE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'FLASH20');

INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount,
                    min_order_amount, start_date, end_date, usage_limit, usage_count,
                    usage_limit_per_user, auto_apply, created_at, updated_at)
SELECT 'VIP1M', 'VIP giam 1 trieu', 'Uu dai dac biet cho thanh vien VIP, giam 1.000.000d',
       'FIXED_AMOUNT', 'ACTIVE', 1000000, NULL, 10000000,
       DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY),
       50, 0, 1, FALSE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'VIP1M');

SET FOREIGN_KEY_CHECKS=1;

-- =============================================================================
-- VERIFICATION
-- =============================================================================
SELECT '========== SEED DATA SUMMARY ==========' AS '';
SELECT 'Users' AS entity, COUNT(*) AS count FROM app_user;
SELECT 'Roles' AS entity, COUNT(*) AS count FROM role;
SELECT 'User Profiles' AS entity, COUNT(*) AS count FROM user_profile;
SELECT 'User Addresses' AS entity, COUNT(*) AS count FROM user_address;
SELECT 'Seller Shops' AS entity, COUNT(*) AS count FROM seller_shop;
SELECT 'Categories' AS entity, COUNT(*) AS count FROM category;
SELECT 'Brands' AS entity, COUNT(*) AS count FROM brand;
SELECT 'Products' AS entity, COUNT(*) AS count FROM product;
SELECT 'Product SKUs' AS entity, COUNT(*) AS count FROM product_sku;
SELECT 'Product Images' AS entity, COUNT(*) AS count FROM product_image;
SELECT 'Coupons' AS entity, COUNT(*) AS count FROM coupon;

SELECT '' AS '';
SELECT '========== TEST ACCOUNTS ==========' AS '';
SELECT u.email, u.full_name, GROUP_CONCAT(r.code) AS roles
FROM app_user u
LEFT JOIN user_role ur ON u.id = ur.user_id
LEFT JOIN role r ON ur.role_id = r.id
WHERE u.email LIKE '%@demo.local'
GROUP BY u.id, u.email, u.full_name
ORDER BY u.email;

SELECT '' AS '';
SELECT '========== PRODUCTS BY SHOP ==========' AS '';
SELECT s.shop_name, COUNT(p.id) AS product_count, SUM(sk.stock_on_hand) AS total_stock
FROM seller_shop s
LEFT JOIN product p ON s.id = p.shop_id
LEFT JOIN product_sku sk ON p.id = sk.product_id
GROUP BY s.id, s.shop_name;

SELECT '' AS '';
SELECT '========== AVAILABLE COUPONS ==========' AS '';
SELECT code, name, type, discount_value, 
       CONCAT(usage_count, '/', IFNULL(usage_limit, 'unlimited')) AS used_limit,
       DATE_FORMAT(end_date, '%Y-%m-%d') AS expires
FROM coupon WHERE status = 'ACTIVE' AND end_date > NOW();
