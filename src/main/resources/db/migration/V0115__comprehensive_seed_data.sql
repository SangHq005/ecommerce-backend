-- =============================================================================
-- COMPREHENSIVE SEED DATA FOR SHOPMART E-COMMERCE
-- =============================================================================
-- Bao gồm: Categories, Brands, Shops, Products, SKUs, Images, Attributes, Reviews
-- =============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- =============================================================================
-- 1. THÊM SELLERS VÀ SHOPS MỚI
-- =============================================================================

-- Thêm sellers mới
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'seller3@gmail.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Xiaomi Store VN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='seller3@gmail.com');

INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'seller4@gmail.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Tech World', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='seller4@gmail.com');

INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'seller5@gmail.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Fashion Hub VN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='seller5@gmail.com');

-- Assign SELLER role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN role r ON r.code = 'SELLER'
WHERE u.email IN ('seller3@gmail.com', 'seller4@gmail.com', 'seller5@gmail.com')
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

-- Create shops
INSERT INTO seller_shop (seller_user_id, shop_name, shop_slug, description, logo_url, banner_url, status, city, address, created_at, updated_at)
SELECT u.id, 'Xiaomi Official Store', 'xiaomi-official', 'Cửa hàng chính hãng Xiaomi - Smartphone, Smart Home, Phụ kiện', 
  'https://images.unsplash.com/photo-1585060544812-6b45742d762f?w=200&h=200&fit=crop', 
  'https://images.unsplash.com/photo-1585060544812-6b45742d762f?w=1200&h=400&fit=crop',
  'ACTIVE', 'Hồ Chí Minh', '123 Nguyễn Huệ, Quận 1', NOW(), NOW()
FROM app_user u WHERE u.email = 'seller3@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM seller_shop s WHERE s.seller_user_id = u.id);

INSERT INTO seller_shop (seller_user_id, shop_name, shop_slug, description, logo_url, banner_url, status, city, address, created_at, updated_at)
SELECT u.id, 'Tech World Store', 'tech-world', 'Thiết bị công nghệ chính hãng - Laptop, Tablet, Phụ kiện Gaming', 
  'https://images.unsplash.com/photo-1518770660439-4636190af475?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200&h=400&fit=crop',
  'ACTIVE', 'Hà Nội', '456 Phố Huế, Hai Bà Trưng', NOW(), NOW()
FROM app_user u WHERE u.email = 'seller4@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM seller_shop s WHERE s.seller_user_id = u.id);

INSERT INTO seller_shop (seller_user_id, shop_name, shop_slug, description, logo_url, banner_url, status, city, address, created_at, updated_at)
SELECT u.id, 'Fashion Hub Vietnam', 'fashion-hub-vn', 'Thời trang nam nữ cao cấp - Quần áo, Giày dép, Phụ kiện', 
  'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=200&h=200&fit=crop',
  'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200&h=400&fit=crop',
  'ACTIVE', 'Đà Nẵng', '789 Trần Phú, Hải Châu', NOW(), NOW()
FROM app_user u WHERE u.email = 'seller5@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM seller_shop s WHERE s.seller_user_id = u.id);

-- =============================================================================
-- 2. THÊM BRANDS MỚI
-- =============================================================================

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Xiaomi', 'xiaomi', 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/ae/Xiaomi_logo_%282021-%29.svg/200px-Xiaomi_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'xiaomi');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'OPPO', 'oppo', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0a/OPPO_LOGO_2019.svg/200px-OPPO_LOGO_2019.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'oppo');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Vivo', 'vivo', 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Vivo_logo_2019.svg/200px-Vivo_logo_2019.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'vivo');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Realme', 'realme', 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Realme_logo.svg/200px-Realme_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'realme');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Dell', 'dell', 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/48/Dell_Logo.svg/200px-Dell_Logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'dell');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'HP', 'hp', 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/ad/HP_logo_2012.svg/200px-HP_logo_2012.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'hp');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Lenovo', 'lenovo', 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/Lenovo_logo_2015.svg/200px-Lenovo_logo_2015.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'lenovo');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'ASUS', 'asus', 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2e/ASUS_Logo.svg/200px-ASUS_Logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'asus');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Acer', 'acer', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/00/Acer_2011.svg/200px-Acer_2011.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'acer');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'MSI', 'msi', 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/13/MSI_Logo.svg/200px-MSI_Logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'msi');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Sony', 'sony', 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Sony_logo.svg/200px-Sony_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'sony');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'LG', 'lg', 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/LG_symbol.svg/200px-LG_symbol.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'lg');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'JBL', 'jbl', 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/JBL_logo.svg/200px-JBL_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'jbl');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Adidas', 'adidas', 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Adidas_Logo.svg/200px-Adidas_Logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'adidas');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Puma', 'puma', 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Puma_logo.svg/200px-Puma_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'puma');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Uniqlo', 'uniqlo', 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/92/UNIQLO_logo.svg/200px-UNIQLO_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'uniqlo');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Anker', 'anker', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Anker_logo.svg/200px-Anker_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'anker');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Logitech', 'logitech', 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Logitech_logo.svg/200px-Logitech_logo.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'logitech');

INSERT INTO brand (name, slug, logo_url, is_active, created_at, updated_at)
SELECT 'Nike', 'nike', 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a6/Logo_NIKE.svg/200px-Logo_NIKE.svg.png', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE slug = 'nike');

-- =============================================================================
-- 3. THÊM CATEGORIES ĐẦY ĐỦ (GỌN GÀNG, KHÔNG TRÙNG LẶP)
-- =============================================================================

-- Level 1 Categories (Parent categories)
INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT * FROM (
    SELECT 'Điện thoại & Phụ kiện' AS name, 'dien-thoai-phu-kien' AS slug, '/dien-thoai-phu-kien' AS path, NULL AS parent_id, TRUE AS is_active, 1 AS sort_order, NOW() AS created_at, NOW() AS updated_at
    UNION ALL SELECT 'Thiết bị điện tử', 'thiet-bi-dien-tu', '/thiet-bi-dien-tu', NULL, TRUE, 3, NOW(), NOW()
    UNION ALL SELECT 'Thời trang Nam', 'thoi-trang-nam', '/thoi-trang-nam', NULL, TRUE, 4, NOW(), NOW()
    UNION ALL SELECT 'Thời trang Nữ', 'thoi-trang-nu', '/thoi-trang-nu', NULL, TRUE, 5, NOW(), NOW()
    UNION ALL SELECT 'Giày dép', 'giay-dep', '/giay-dep', NULL, TRUE, 6, NOW(), NOW()
    UNION ALL SELECT 'Đồng hồ', 'dong-ho', '/dong-ho', NULL, TRUE, 7, NOW(), NOW()
    UNION ALL SELECT 'Nhà cửa & Đời sống', 'nha-cua-doi-song', '/nha-cua-doi-song', NULL, TRUE, 8, NOW(), NOW()
) AS new_cats
WHERE NOT EXISTS (SELECT 1 FROM category c WHERE c.slug = new_cats.slug);

-- Level 2 Categories - Điện thoại & Phụ kiện
INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT cat.name, cat.slug, cat.path, p.id, cat.is_active, cat.sort_order, cat.created_at, cat.updated_at
FROM (
    SELECT 'Điện thoại Smartphone' AS name, 'dien-thoai-smartphone' AS slug, '/dien-thoai-phu-kien/dien-thoai-smartphone' AS path, TRUE AS is_active, 1 AS sort_order, NOW() AS created_at, NOW() AS updated_at
    UNION ALL
    SELECT 'Phụ kiện điện thoại', 'phu-kien-dien-thoai', '/dien-thoai-phu-kien/phu-kien-dien-thoai', TRUE, 2, NOW(), NOW()
) AS cat
CROSS JOIN category p
WHERE p.slug = 'dien-thoai-phu-kien'
  AND NOT EXISTS (SELECT 1 FROM category c WHERE c.slug = cat.slug);

-- Level 2 Categories - Máy tính & Laptop
INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT cat.name, cat.slug, cat.path, p.id, cat.is_active, cat.sort_order, cat.created_at, cat.updated_at
FROM (
    SELECT 'Laptop' AS name, 'laptop-category' AS slug, '/may-tinh-laptop/laptop' AS path, TRUE AS is_active, 1 AS sort_order, NOW() AS created_at, NOW() AS updated_at
    UNION ALL
    SELECT 'PC & Máy tính để bàn', 'pc-may-tinh-de-ban', '/may-tinh-laptop/pc', TRUE, 2, NOW(), NOW()
    UNION ALL
    SELECT 'Linh kiện máy tính', 'linh-kien-may-tinh', '/may-tinh-laptop/linh-kien', TRUE, 3, NOW(), NOW()
) AS cat
CROSS JOIN category p
WHERE p.slug = 'may-tinh-laptop'
  AND NOT EXISTS (SELECT 1 FROM category c WHERE c.slug = cat.slug);

-- Level 2 Categories - Thiết bị điện tử
INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT cat.name, cat.slug, cat.path, p.id, cat.is_active, cat.sort_order, cat.created_at, cat.updated_at
FROM (
    SELECT 'Tai nghe' AS name, 'tai-nghe' AS slug, '/thiet-bi-dien-tu/tai-nghe' AS path, TRUE AS is_active, 1 AS sort_order, NOW() AS created_at, NOW() AS updated_at
    UNION ALL
    SELECT 'Loa & Âm thanh', 'loa-am-thanh', '/thiet-bi-dien-tu/loa-am-thanh', TRUE, 2, NOW(), NOW()
    UNION ALL
    SELECT 'Smartwatch', 'smartwatch', '/thiet-bi-dien-tu/smartwatch', TRUE, 3, NOW(), NOW()
) AS cat
CROSS JOIN category p
WHERE p.slug = 'thiet-bi-dien-tu'
  AND NOT EXISTS (SELECT 1 FROM category c WHERE c.slug = cat.slug);

-- Level 2 Categories - Thời trang Nam
INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT cat.name, cat.slug, cat.path, p.id, cat.is_active, cat.sort_order, cat.created_at, cat.updated_at
FROM (
    SELECT 'Áo nam' AS name, 'ao-nam' AS slug, '/thoi-trang-nam/ao-nam' AS path, TRUE AS is_active, 1 AS sort_order, NOW() AS created_at, NOW() AS updated_at
    UNION ALL
    SELECT 'Quần nam', 'quan-nam', '/thoi-trang-nam/quan-nam', TRUE, 2, NOW(), NOW()
) AS cat
CROSS JOIN category p
WHERE p.slug = 'thoi-trang-nam'
  AND NOT EXISTS (SELECT 1 FROM category c WHERE c.slug = cat.slug);

-- Level 2 Categories - Giày dép
INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT cat.name, cat.slug, cat.path, p.id, cat.is_active, cat.sort_order, cat.created_at, cat.updated_at
FROM (
    SELECT 'Giày thể thao' AS name, 'giay-the-thao' AS slug, '/giay-dep/giay-the-thao' AS path, TRUE AS is_active, 1 AS sort_order, NOW() AS created_at, NOW() AS updated_at
    UNION ALL
    SELECT 'Giày da', 'giay-da', '/giay-dep/giay-da', TRUE, 2, NOW(), NOW()
) AS cat
CROSS JOIN category p
WHERE p.slug = 'giay-dep'
  AND NOT EXISTS (SELECT 1 FROM category c WHERE c.slug = cat.slug);

-- =============================================================================
-- 4. SẢN PHẨM ĐIỆN THOẠI (với đầy đủ thông tin)
-- =============================================================================

-- iPhone 15 Pro Max 256GB
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'iPhone 15 Pro Max 256GB - Chính hãng VN/A',
  'iphone-15-pro-max-256gb-chinh-hang',
  '<h2>iPhone 15 Pro Max - Đỉnh cao công nghệ từ Apple</h2>
<p>iPhone 15 Pro Max là smartphone cao cấp nhất của Apple với thiết kế titan sang trọng, chip A17 Pro mạnh mẽ nhất và camera zoom quang học 5x ấn tượng.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Chip A17 Pro:</strong> Hiệu năng đỉnh cao với công nghệ 3nm đầu tiên trên iPhone</li>
<li><strong>Camera 48MP:</strong> Hệ thống camera chuyên nghiệp với zoom quang 5x</li>
<li><strong>Titan Grade 5:</strong> Khung titan cao cấp, nhẹ và bền bỉ</li>
<li><strong>Action Button:</strong> Nút tác vụ mới thay thế công tắc im lặng</li>
<li><strong>USB-C:</strong> Cổng sạc chuẩn USB-C tiện lợi</li>
</ul>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070795/8ba558e80bb840e0d22ec086ece78410_tbvs83.jpg',
  34990000, 36990000, 150, 'VND', 'ACTIVE', 4.9, 256, 1250, 221, TRUE, 95, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND p.shop_id = s.id);

-- Samsung Galaxy S24 Ultra
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Samsung Galaxy S24 Ultra 256GB - Chính hãng',
  'samsung-galaxy-s24-ultra-256gb',
  '<h2>Samsung Galaxy S24 Ultra - Galaxy AI đã đến!</h2>
<p>Galaxy S24 Ultra là flagship Android đỉnh cao với trí tuệ nhân tạo Galaxy AI, bút S-Pen tích hợp và camera 200MP siêu chi tiết.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Galaxy AI:</strong> Circle to Search, Live Translate, Note Assist</li>
<li><strong>Camera 200MP:</strong> Chụp ảnh siêu chi tiết, zoom 100x</li>
<li><strong>Màn hình QHD+:</strong> 6.8 inch Dynamic AMOLED 2X</li>
<li><strong>S-Pen tích hợp:</strong> Viết, vẽ, điều khiển từ xa</li>
<li><strong>Pin 5000mAh:</strong> Dùng cả ngày, sạc siêu nhanh 45W</li>
</ul>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070835/2f4543e8b063f32c4346ace156929ceb_crht0l.jpg',
  31990000, 33990000, 200, 'VND', 'ACTIVE', 4.8, 189, 980, 233, TRUE, 92, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Samsung Hub' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'samsung'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND p.shop_id = s.id);

-- Xiaomi 14 Ultra
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Xiaomi 14 Ultra 512GB - Camera Leica siêu đỉnh',
  'xiaomi-14-ultra-512gb-leica',
  '<h2>Xiaomi 14 Ultra - Đỉnh cao nhiếp ảnh di động</h2>
<p>Xiaomi 14 Ultra hợp tác cùng Leica mang đến trải nghiệm chụp ảnh chuyên nghiệp nhất trên smartphone với ống kính Summilux.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Camera Leica Summilux:</strong> 4 camera 50MP, ống kính khẩu độ biến đổi</li>
<li><strong>Chip Snapdragon 8 Gen 3:</strong> Hiệu năng mạnh mẽ nhất Android</li>
<li><strong>Màn hình 2K LTPO AMOLED:</strong> 6.73 inch, 120Hz, 3000 nits</li>
<li><strong>Sạc siêu nhanh 90W:</strong> Đầy pin trong 30 phút</li>
</ul>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070925/86269ac3bf4f93b11581c15d653637a9_bi0zjp.jpg',
  29990000, 32990000, 80, 'VND', 'ACTIVE', 4.7, 145, 650, 220, TRUE, 90, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Xiaomi Official Store' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'xiaomi'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND p.shop_id = s.id);

-- OPPO Find X7 Ultra
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'OPPO Find X7 Ultra 256GB - Hasselblad Camera',
  'oppo-find-x7-ultra-256gb',
  '<h2>OPPO Find X7 Ultra - Camera Hasselblad đỉnh cao</h2>
<p>Find X7 Ultra với hệ thống camera kép Hasselblad mang đến chất lượng ảnh chuyên nghiệp và khả năng zoom quang học ấn tượng.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Camera Hasselblad:</strong> Cảm biến 1 inch LYT-900, 50MP x2</li>
<li><strong>Zoom Periscope kép:</strong> 3x và 6x quang học</li>
<li><strong>Chip Snapdragon 8 Gen 3:</strong> Hiệu năng flagship</li>
<li><strong>Pin 5400mAh:</strong> Sạc nhanh 100W SUPERVOOC</li>
</ul>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071005/ff0091afa5382c9662a0e7a87f42a164_nmb7ci.jpg',
  27990000, 29990000, 60, 'VND', 'ACTIVE', 4.6, 98, 420, 221, FALSE, 88, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'oppo'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND p.shop_id = s.id);

-- Xiaomi Redmi Note 13 Pro+
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Xiaomi Redmi Note 13 Pro+ 5G 256GB',
  'xiaomi-redmi-note-13-pro-plus-5g',
  '<h2>Redmi Note 13 Pro+ - Camera 200MP giá rẻ</h2>
<p>Redmi Note 13 Pro+ mang đến camera 200MP đầu tiên trong phân khúc tầm trung, cùng màn hình AMOLED 120Hz và sạc siêu nhanh 120W.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Camera 200MP:</strong> Cảm biến Samsung HP3, chụp siêu chi tiết</li>
<li><strong>Màn hình AMOLED:</strong> 6.67 inch, 120Hz, 1800 nits</li>
<li><strong>Sạc siêu nhanh 120W:</strong> Đầy pin trong 19 phút</li>
<li><strong>Kháng nước IP68:</strong> Bảo vệ toàn diện</li>
</ul>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071051/2bbde907985112c55b1438d288b997d0_szboav.jpg',
  9990000, 10990000, 300, 'VND', 'ACTIVE', 4.5, 312, 2500, 204, TRUE, 85, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Xiaomi Official Store' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'xiaomi'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND p.shop_id = s.id);

-- Samsung Galaxy A55 5G
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Samsung Galaxy A55 5G 128GB',
  'samsung-galaxy-a55-5g-128gb',
  '<h2>Galaxy A55 5G - Flagship trải nghiệm, giá tầm trung</h2>
<p>Galaxy A55 5G mang đến trải nghiệm cao cấp với thiết kế khung nhôm, màn hình Super AMOLED 120Hz và camera OIS chống rung quang học.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Thiết kế cao cấp:</strong> Khung nhôm, kháng nước IP67</li>
<li><strong>Màn hình Super AMOLED:</strong> 6.6 inch, FHD+, 120Hz</li>
<li><strong>Camera 50MP OIS:</strong> Chống rung quang học</li>
<li><strong>Pin 5000mAh:</strong> Dùng cả ngày</li>
</ul>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071087/2ffb48558701e9b9449cb16d14e3de72_kfzphc.jpg',
  9490000, 10490000, 250, 'VND', 'ACTIVE', 4.4, 245, 1800, 213, FALSE, 82, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Samsung Hub' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'samsung'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND p.shop_id = s.id);

-- iPhone 15 128GB
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'iPhone 15 128GB - Chính hãng VN/A',
  'iphone-15-128gb-chinh-hang',
  '<h2>iPhone 15 - Dynamic Island cho tất cả</h2>
<p>iPhone 15 với Dynamic Island, camera 48MP và cổng USB-C mang đến trải nghiệm iPhone hoàn hảo với giá hợp lý.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Dynamic Island:</strong> Tương tác thông minh với thông báo</li>
<li><strong>Camera 48MP:</strong> Chụp ảnh sắc nét, portrait chuyên nghiệp</li>
<li><strong>Chip A16 Bionic:</strong> Hiệu năng mạnh mẽ</li>
<li><strong>USB-C:</strong> Sạc và truyền dữ liệu tiện lợi</li>
</ul>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071143/22119c436265bb773a93950e73970a5e_jtnfvy.jpg',
  22990000, 24990000, 200, 'VND', 'ACTIVE', 4.7, 178, 920, 171, FALSE, 88, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND p.shop_id = s.id);

-- =============================================================================
-- 5. SẢN PHẨM LAPTOP
-- =============================================================================

-- MacBook Pro 14 M3 Pro
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'MacBook Pro 14 inch M3 Pro 512GB - Chính hãng Apple',
  'macbook-pro-14-m3-pro-512gb',
  '<h2>MacBook Pro 14 M3 Pro - Sức mạnh chuyên nghiệp</h2>
<p>MacBook Pro với chip M3 Pro mang đến hiệu năng đột phá cho các tác vụ chuyên nghiệp như render video, xử lý ảnh và lập trình.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Chip M3 Pro:</strong> CPU 11 nhân, GPU 14 nhân</li>
<li><strong>Màn hình Liquid Retina XDR:</strong> 14.2 inch, 120Hz ProMotion</li>
<li><strong>RAM 18GB:</strong> Bộ nhớ hợp nhất siêu nhanh</li>
<li><strong>SSD 512GB:</strong> Tốc độ đọc/ghi cực nhanh</li>
<li><strong>Pin 17 giờ:</strong> Làm việc cả ngày không lo hết pin</li>
</ul>',
  'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80',
  49990000, 52990000, 50, 'VND', 'ACTIVE', 4.9, 178, 450, 1610, TRUE, 95, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official' AND c.slug = 'laptop-category' AND b.slug = 'apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND p.shop_id = s.id);

-- Dell XPS 15 9530
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Dell XPS 15 9530 - Core i7 Gen 13, RTX 4060',
  'dell-xps-15-9530-i7-rtx4060',
  '<h2>Dell XPS 15 - Laptop Windows cao cấp nhất</h2>
<p>Dell XPS 15 với thiết kế InfinityEdge không viền, Core i7 thế hệ 13 và RTX 4060 cho hiệu năng gaming và đồ họa chuyên nghiệp.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Core i7-13700H:</strong> 14 nhân, 20 luồng, hiệu năng cao</li>
<li><strong>RTX 4060 8GB:</strong> Đồ họa rời mạnh mẽ</li>
<li><strong>Màn hình OLED 3.5K:</strong> 15.6 inch, 100% DCI-P3</li>
<li><strong>RAM 32GB DDR5:</strong> Đa nhiệm mượt mà</li>
<li><strong>SSD 1TB:</strong> NVMe PCIe 4.0</li>
</ul>',
  'https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=800&q=80',
  45990000, 49990000, 30, 'VND', 'ACTIVE', 4.7, 89, 280, 1860, TRUE, 90, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'laptop-category' AND b.slug = 'dell'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND p.shop_id = s.id);

-- ASUS ROG Strix G16 Gaming
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'ASUS ROG Strix G16 - Core i9, RTX 4070',
  'asus-rog-strix-g16-i9-rtx4070',
  '<h2>ASUS ROG Strix G16 - Gaming laptop đỉnh cao</h2>
<p>ROG Strix G16 với Core i9, RTX 4070 và màn hình 240Hz mang đến trải nghiệm gaming không giới hạn.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Core i9-13980HX:</strong> 24 nhân, hiệu năng khủng</li>
<li><strong>RTX 4070 8GB:</strong> Ray Tracing, DLSS 3.0</li>
<li><strong>Màn hình 240Hz:</strong> 16 inch QHD+, 3ms</li>
<li><strong>RAM 32GB DDR5:</strong> 4800MHz</li>
<li><strong>Tản nhiệt ROG:</strong> Liquid Metal, 4 quạt</li>
</ul>',
  'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=800&q=80',
  52990000, 57990000, 25, 'VND', 'ACTIVE', 4.8, 156, 380, 2500, TRUE, 92, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'laptop-category' AND b.slug = 'asus'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND p.shop_id = s.id);

-- Lenovo ThinkPad X1 Carbon
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Lenovo ThinkPad X1 Carbon Gen 11 - Core i7 vPro',
  'lenovo-thinkpad-x1-carbon-gen11',
  '<h2>ThinkPad X1 Carbon Gen 11 - Laptop doanh nhân đỉnh cao</h2>
<p>ThinkPad X1 Carbon với sợi carbon siêu nhẹ, bàn phím huyền thoại và bảo mật doanh nghiệp toàn diện.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Core i7-1365U vPro:</strong> Bảo mật và quản lý doanh nghiệp</li>
<li><strong>Siêu nhẹ 1.12kg:</strong> Sợi carbon cao cấp</li>
<li><strong>Màn hình 2.8K OLED:</strong> 14 inch, 100% DCI-P3</li>
<li><strong>RAM 32GB LPDDR5:</strong> Tích hợp</li>
<li><strong>Bàn phím ThinkPad:</strong> Hành trình 1.5mm, backlit</li>
</ul>',
  'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&q=80',
  42990000, 45990000, 40, 'VND', 'ACTIVE', 4.6, 67, 190, 1120, FALSE, 88, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'laptop-category' AND b.slug = 'lenovo'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND p.shop_id = s.id);

-- MacBook Air M3
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'MacBook Air 15 inch M3 256GB - Midnight',
  'macbook-air-15-m3-256gb',
  '<h2>MacBook Air 15 M3 - Mỏng nhẹ, mạnh mẽ</h2>
<p>MacBook Air 15 inch với chip M3 mang đến hiệu năng vượt trội trong thiết kế siêu mỏng không quạt.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Chip M3:</strong> CPU 8 nhân, GPU 10 nhân</li>
<li><strong>Màn hình 15.3 inch:</strong> Liquid Retina, True Tone</li>
<li><strong>Pin 18 giờ:</strong> Dùng cả ngày</li>
<li><strong>Thiết kế không quạt:</strong> Yên tĩnh tuyệt đối</li>
</ul>',
  'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&q=80',
  32990000, 34990000, 80, 'VND', 'ACTIVE', 4.8, 234, 890, 1510, TRUE, 90, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official' AND c.slug = 'laptop-category' AND b.slug = 'apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'macbook-air-15-m3-256gb' AND p.shop_id = s.id);

-- =============================================================================
-- 6. TAI NGHE & SMARTWATCH
-- =============================================================================

-- Apple AirPods Pro 2
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Apple AirPods Pro 2 USB-C - Chính hãng',
  'apple-airpods-pro-2-usb-c',
  '<h2>AirPods Pro 2 - Tai nghe True Wireless tốt nhất</h2>
<p>AirPods Pro 2 với chip H2, chống ồn chủ động thế hệ mới và âm thanh Spatial Audio cá nhân hóa.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Chip H2:</strong> ANC mạnh gấp 2 lần, Adaptive Transparency</li>
<li><strong>Spatial Audio:</strong> Âm thanh không gian cá nhân hóa</li>
<li><strong>Pin 30 giờ:</strong> 6 giờ tai nghe + 24 giờ case</li>
<li><strong>IP54:</strong> Kháng nước, kháng bụi</li>
</ul>',
  'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=800&q=80',
  6290000, 6990000, 200, 'VND', 'ACTIVE', 4.8, 456, 3200, 50, TRUE, 92, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official' AND c.slug = 'tai-nghe' AND b.slug = 'apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'apple-airpods-pro-2-usb-c' AND p.shop_id = s.id);

-- Samsung Galaxy Buds3 Pro
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Samsung Galaxy Buds3 Pro - ANC thông minh',
  'samsung-galaxy-buds3-pro',
  '<h2>Galaxy Buds3 Pro - Tai nghe AI thông minh</h2>
<p>Galaxy Buds3 Pro với thiết kế mới, ANC thích ứng thông minh và tích hợp Galaxy AI.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Thiết kế Blade Lights:</strong> LED RGB cá tính</li>
<li><strong>ANC thích ứng:</strong> Tự động điều chỉnh theo môi trường</li>
<li><strong>Galaxy AI:</strong> Interpreter, Note Assistant</li>
<li><strong>Pin 30 giờ:</strong> 7 giờ tai nghe + 23 giờ case</li>
</ul>',
  'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&q=80',
  5490000, 5990000, 150, 'VND', 'ACTIVE', 4.6, 234, 1800, 48, TRUE, 88, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Samsung Hub' AND c.slug = 'tai-nghe' AND b.slug = 'samsung'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'samsung-galaxy-buds3-pro' AND p.shop_id = s.id);

-- Apple Watch Series 9
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Apple Watch Series 9 GPS 45mm - Nhôm',
  'apple-watch-series-9-gps-45mm',
  '<h2>Apple Watch Series 9 - Smartwatch thông minh nhất</h2>
<p>Apple Watch Series 9 với chip S9, Double Tap gesture và màn hình sáng nhất lịch sử.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Chip S9:</strong> Nhanh hơn 60%, Neural Engine mới</li>
<li><strong>Double Tap:</strong> Điều khiển bằng cử chỉ tay</li>
<li><strong>Màn hình 2000 nits:</strong> Sáng gấp 2 lần, luôn bật</li>
<li><strong>Sức khỏe toàn diện:</strong> ECG, SpO2, nhiệt độ</li>
</ul>',
  'https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d?w=800&q=80',
  11490000, 12490000, 100, 'VND', 'ACTIVE', 4.7, 189, 980, 38, TRUE, 90, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official' AND c.slug = 'smartwatch' AND b.slug = 'apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'apple-watch-series-9-gps-45mm' AND p.shop_id = s.id);

-- Samsung Galaxy Watch 6 Classic
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Samsung Galaxy Watch 6 Classic 47mm',
  'samsung-galaxy-watch-6-classic-47mm',
  '<h2>Galaxy Watch 6 Classic - Đồng hồ xoay vòng</h2>
<p>Galaxy Watch 6 Classic với vòng xoay bezel kinh điển, màn hình lớn hơn và theo dõi sức khỏe toàn diện.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Vòng xoay Bezel:</strong> Điều khiển trực quan</li>
<li><strong>Màn hình AMOLED:</strong> 1.5 inch, viền mỏng hơn 30%</li>
<li><strong>BioActive Sensor:</strong> ECG, huyết áp, SpO2</li>
<li><strong>Pin 40 giờ:</strong> Wear OS, Google Assistant</li>
</ul>',
  'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=800&q=80',
  9990000, 10990000, 80, 'VND', 'ACTIVE', 4.5, 145, 720, 59, FALSE, 85, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Samsung Hub' AND c.slug = 'smartwatch' AND b.slug = 'samsung'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'samsung-galaxy-watch-6-classic-47mm' AND p.shop_id = s.id);

-- =============================================================================
-- 7. SẢN PHẨM THỜI TRANG & GIÀY DÉP
-- =============================================================================

-- Nike Air Force 1 '07
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Nike Air Force 1 07 - Triple White',
  'nike-air-force-1-07-triple-white',
  '<h2>Nike Air Force 1 07 - Huyền thoại không tuổi</h2>
<p>Air Force 1 07 phiên bản Triple White kinh điển, biểu tượng của sneaker văn hóa toàn cầu.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Thiết kế kinh điển:</strong> Silhouette huyền thoại từ 1982</li>
<li><strong>Full leather:</strong> Da thật cao cấp</li>
<li><strong>Đế Air:</strong> Êm ái, bền bỉ</li>
<li><strong>Phối đồ dễ dàng:</strong> Phù hợp mọi outfit</li>
</ul>',
  'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80',
  2989000, 3289000, 200, 'VND', 'ACTIVE', 4.8, 567, 4500, 400, TRUE, 90, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Fashion Hub Vietnam' AND c.slug = 'giay-the-thao' AND b.slug = 'nike'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'nike-air-force-1-07-triple-white' AND p.shop_id = s.id);

-- Adidas Ultraboost 23
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Adidas Ultraboost 23 - Core Black',
  'adidas-ultraboost-23-core-black',
  '<h2>Adidas Ultraboost 23 - Chạy bộ đỉnh cao</h2>
<p>Ultraboost 23 với công nghệ BOOST mới, đệm Linear Energy Push mang đến trải nghiệm chạy bộ thoải mái nhất.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>BOOST mới:</strong> Hoàn trả năng lượng tốt hơn</li>
<li><strong>Linear Energy Push:</strong> Đế giữa tối ưu</li>
<li><strong>Primeknit+:</strong> Upper thoáng khí, ôm chân</li>
<li><strong>Continental Rubber:</strong> Đế ngoài bám tốt</li>
</ul>',
  'https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?w=800&q=80',
  4590000, 5090000, 150, 'VND', 'ACTIVE', 4.7, 345, 2800, 320, TRUE, 88, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Fashion Hub Vietnam' AND c.slug = 'giay-the-thao' AND b.slug = 'adidas'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'adidas-ultraboost-23-core-black' AND p.shop_id = s.id);

-- Áo Polo Uniqlo
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Áo Polo Uniqlo DRY-EX - Navy',
  'ao-polo-uniqlo-dry-ex-navy',
  '<h2>Áo Polo Uniqlo DRY-EX - Thoáng mát tối ưu</h2>
<p>Áo Polo với công nghệ DRY-EX thấm hút mồ hôi, khô nhanh, phù hợp thời tiết nóng.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>DRY-EX:</strong> Thấm hút mồ hôi, khô nhanh</li>
<li><strong>Chất liệu nhẹ:</strong> Thoáng khí, co giãn</li>
<li><strong>Thiết kế basic:</strong> Dễ phối, thanh lịch</li>
<li><strong>Bền màu:</strong> Giặt nhiều không phai</li>
</ul>',
  'https://images.unsplash.com/photo-1621072156002-e2fccdc0b176?w=800&q=80',
  499000, 599000, 500, 'VND', 'ACTIVE', 4.5, 234, 5600, 200, FALSE, 85, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Fashion Hub Vietnam' AND c.slug = 'ao-nam' AND b.slug = 'uniqlo'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'ao-polo-uniqlo-dry-ex-navy' AND p.shop_id = s.id);

-- =============================================================================
-- 8. PHỤ KIỆN CÔNG NGHỆ
-- =============================================================================

-- Anker PowerCore 10000
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Anker PowerCore 10000mAh - Sạc nhanh 22.5W',
  'anker-powercore-10000mah-225w',
  '<h2>Anker PowerCore 10000 - Pin dự phòng nhỏ gọn</h2>
<p>Pin dự phòng 10000mAh nhỏ gọn với sạc nhanh 22.5W, đủ sạc iPhone 2 lần.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Sạc nhanh 22.5W:</strong> Hỗ trợ PD, QC 3.0</li>
<li><strong>Dung lượng 10000mAh:</strong> Sạc iPhone 2 lần</li>
<li><strong>Nhỏ gọn:</strong> Vừa túi quần</li>
<li><strong>An toàn:</strong> MultiProtect 10 lớp bảo vệ</li>
</ul>',
  'https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?w=800&q=80',
  490000, 590000, 300, 'VND', 'ACTIVE', 4.6, 456, 8900, 180, FALSE, 85, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'phu-kien-dien-thoai' AND b.slug = 'anker'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'anker-powercore-10000mah-225w' AND p.shop_id = s.id);

-- Logitech MX Master 3S
INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Logitech MX Master 3S - Chuột không dây cao cấp',
  'logitech-mx-master-3s-wireless',
  '<h2>Logitech MX Master 3S - Chuột làm việc tốt nhất</h2>
<p>MX Master 3S với sensor 8000 DPI, click im lặng và cuộn MagSpeed siêu mượt.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Sensor 8000 DPI:</strong> Tracking trên mọi bề mặt</li>
<li><strong>Click im lặng:</strong> Giảm 90% tiếng ồn</li>
<li><strong>MagSpeed Scroll:</strong> Cuộn 1000 dòng/giây</li>
<li><strong>Kết nối 3 thiết bị:</strong> Flow chuyển đổi mượt</li>
<li><strong>Pin 70 ngày:</strong> Sạc nhanh USB-C</li>
</ul>',
  'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=800&q=80',
  2490000, 2790000, 100, 'VND', 'ACTIVE', 4.8, 234, 1200, 141, TRUE, 92, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Tech World Store' AND c.slug = 'linh-kien-may-tinh' AND b.slug = 'logitech'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'logitech-mx-master-3s-wireless' AND p.shop_id = s.id);

-- =============================================================================
-- 9. TẠO PRODUCT IMAGES
-- =============================================================================

-- Insert images cho tất cả products chưa có image
INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, p.main_image_url, 1 FROM product p
WHERE NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 1);

-- =============================================================================
-- 10. TẠO PRODUCT SKU
-- =============================================================================

-- SKU mặc định cho tất cả sản phẩm chưa có SKU
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, CONCAT('SKU-', p.id), p.price, p.stock_quantity, 0, 'default', MD5(CONCAT('SKU-', p.id)), NOW(), NOW()
FROM product p
WHERE NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.product_id = p.id);

-- =============================================================================
-- 11. TẠO PRODUCT ATTRIBUTES (THÔNG SỐ KỸ THUẬT)
-- =============================================================================

-- Attribute values cho iPhone 15 Pro Max
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.7 inch', '6.7', 6.7, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Super Retina XDR OLED', 'Super Retina XDR OLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Apple A17 Pro', 'Apple A17 Pro', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '8 GB', '8', 8, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '256 GB', '256', 256, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '4441 mAh', '4441', 4441, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '48MP + 12MP + 12MP Telephoto 5x', '48MP + 12MP + 12MP', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'iOS 17', 'iOS 17', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho Samsung S24 Ultra  
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.8 inch', '6.8', 6.8, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Dynamic AMOLED 2X', 'Dynamic AMOLED 2X', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Snapdragon 8 Gen 3 for Galaxy', 'Snapdragon 8 Gen 3', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '12 GB', '12', 12, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '256 GB', '256', 256, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '5000 mAh', '5000', 5000, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '200MP + 50MP + 12MP + 10MP', '200MP + 50MP + 12MP + 10MP', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Android 14, One UI 6.1', 'Android 14', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho Xiaomi 14 Ultra
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.73 inch', '6.73', 6.73, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'LTPO AMOLED 2K', 'LTPO AMOLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Snapdragon 8 Gen 3', 'Snapdragon 8 Gen 3', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '16 GB', '16', 16, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '512 GB', '512', 512, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '5300 mAh', '5300', 5300, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '50MP Leica Summilux x4', '50MP Leica', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Android 14, MIUI 15', 'Android 14', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-14-ultra-512gb-leica' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho OPPO Find X7 Ultra
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.78 inch', '6.78', 6.78, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'LTPO AMOLED 2K', 'LTPO AMOLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Snapdragon 8 Gen 3', 'Snapdragon 8 Gen 3', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '16 GB', '16', 16, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '256 GB', '256', 256, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '5400 mAh', '5400', 5400, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '100 W', '100', 100, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'cong-suat-sac'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '50MP Hasselblad LYT-900 x2 + 50MP + 50MP', '50MP Hasselblad', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Android 14, ColorOS 14', 'Android 14', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'oppo-find-x7-ultra-256gb' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho Xiaomi Redmi Note 13 Pro+
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.67 inch', '6.67', 6.67, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'AMOLED 120Hz', 'AMOLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '120 Hz', '120', 120, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'tan-so-quet'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'MediaTek Dimensity 7200 Ultra', 'Dimensity 7200 Ultra', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '12 GB', '12', 12, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '256 GB', '256', 256, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '5000 mAh', '5000', 5000, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '120 W', '120', 120, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'cong-suat-sac'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '200MP Samsung HP3', '200MP', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'IP68', 'IP68', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'chuan-khang-nuoc'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Android 14, MIUI 15', 'Android 14', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho Samsung Galaxy A55 5G
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.6 inch', '6.6', 6.6, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Super AMOLED FHD+', 'Super AMOLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '120 Hz', '120', 120, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'tan-so-quet'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Exynos 1480', 'Exynos 1480', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '8 GB', '8', 8, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '128 GB', '128', 128, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '5000 mAh', '5000', 5000, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '50MP OIS + 12MP + 5MP', '50MP OIS', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'IP67', 'IP67', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'chuan-khang-nuoc'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Android 14, One UI 6.1', 'Android 14', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'samsung-galaxy-a55-5g-128gb' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho iPhone 15 128GB
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6.1 inch', '6.1', 6.1, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Super Retina XDR OLED', 'Super Retina XDR OLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Apple A16 Bionic', 'Apple A16 Bionic', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '6 GB', '6', 6, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '128 GB', '128', 128, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '3349 mAh', '3349', 3349, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'dung-luong-pin'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, '48MP + 12MP', '48MP + 12MP', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'camera-chinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'iOS 17', 'iOS 17', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'iphone-15-128gb-chinh-hang' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho MacBook Pro 14 M3 Pro
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '14.2 inch', '14.2', 14.2, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Liquid Retina XDR', 'Liquid Retina XDR', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '120 Hz', '120', 120, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'tan-so-quet'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Apple M3 Pro', 'Apple M3 Pro', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '18 GB', '18', 18, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '512 GB', '512', 512, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'macOS Sonoma', 'macOS Sonoma', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '1610 gram', '1610', 1610, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND a.slug = 'trong-luong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho Dell XPS 15 9530
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '15.6 inch', '15.6', 15.6, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'OLED 3.5K', 'OLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Intel Core i7-13700H', 'Core i7-13700H', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'NVIDIA RTX 4060 8GB', 'RTX 4060', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'gpu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '32 GB', '32', 32, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '1 TB', '1024', 1024, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Windows 11', 'Windows 11', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '1860 gram', '1860', 1860, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'dell-xps-15-9530-i7-rtx4060' AND a.slug = 'trong-luong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho ASUS ROG Strix G16
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '16 inch', '16', 16, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'QHD+ IPS', 'IPS', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '240 Hz', '240', 240, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'tan-so-quet'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Intel Core i9-13980HX', 'Core i9-13980HX', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'NVIDIA RTX 4070 8GB', 'RTX 4070', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'gpu'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '32 GB', '32', 32, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Windows 11', 'Windows 11', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '2500 gram', '2500', 2500, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND a.slug = 'trong-luong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho Lenovo ThinkPad X1 Carbon
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '14 inch', '14', 14, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'OLED 2.8K', 'OLED', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Intel Core i7-1365U vPro', 'Core i7-1365U', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '32 GB', '32', 32, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Windows 11 Pro', 'Windows 11 Pro', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '1120 gram', '1120', 1120, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'lenovo-thinkpad-x1-carbon-gen11' AND a.slug = 'trong-luong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- Attribute values cho MacBook Air 15 M3
INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '15.3 inch', '15.3', 15.3, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'kich-thuoc-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Liquid Retina', 'Liquid Retina', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'cong-nghe-man-hinh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'Apple M3', 'Apple M3', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'chip-xu-ly'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '8 GB', '8', 8, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'ram'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '256 GB', '256', 256, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'bo-nho-trong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, created_at, updated_at)
SELECT p.id, a.id, 'macOS Sonoma', 'macOS Sonoma', NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'he-dieu-hanh'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

INSERT INTO product_attribute_value (product_id, attribute_id, display_value, value_text, value_number, created_at, updated_at)
SELECT p.id, a.id, '1510 gram', '1510', 1510, NOW(), NOW()
FROM product p, attribute a
WHERE p.slug = 'macbook-air-15-m3-256gb' AND a.slug = 'trong-luong'
  AND NOT EXISTS (SELECT 1 FROM product_attribute_value pav WHERE pav.product_id = p.id AND pav.attribute_id = a.id);

-- =============================================================================
-- 12. TẠO REVIEWS
-- =============================================================================

-- Reviews cho iPhone 15 Pro Max
INSERT INTO review (product_id, user_id, rating, comment, images, status, created_at)
SELECT p.id, u.id, 5, 'Sản phẩm tuyệt vời! Camera chụp đẹp, hiệu năng mạnh mẽ. Rất hài lòng với iPhone 15 Pro Max.', 
  '["https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=400"]', 'APPROVED', DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM product p, app_user u
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND u.email = 'client1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id AND r.comment LIKE '%Camera chụp đẹp%');

INSERT INTO review (product_id, user_id, rating, comment, status, created_at)
SELECT p.id, u.id, 5, 'Đóng gói cẩn thận, giao hàng nhanh. Action Button rất tiện lợi. 10/10!', 
  'APPROVED', DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM product p, app_user u
WHERE p.slug = 'iphone-15-pro-max-256gb-chinh-hang' AND u.email = 'client2@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id AND r.comment LIKE '%Action Button%');

-- Reviews cho Samsung S24 Ultra
INSERT INTO review (product_id, user_id, rating, comment, images, status, created_at)
SELECT p.id, u.id, 5, 'Galaxy AI rất hay! Tính năng Circle to Search giúp tìm kiếm siêu nhanh. Camera 200MP chụp cực nét.', 
  '["https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=400"]', 'APPROVED', DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM product p, app_user u
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND u.email = 'client1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id AND r.comment LIKE '%Galaxy AI%');

INSERT INTO review (product_id, user_id, rating, comment, status, created_at)
SELECT p.id, u.id, 4, 'Máy đẹp, cấu hình khủng. Chỉ tiếc là hơi nặng vì có S-Pen. Nhưng nhìn chung rất ưng.', 
  'APPROVED', DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM product p, app_user u
WHERE p.slug = 'samsung-galaxy-s24-ultra-256gb' AND u.email = 'client2@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id AND r.comment LIKE '%S-Pen%');

-- Reviews cho các sản phẩm khác
INSERT INTO review (product_id, user_id, rating, comment, status, created_at)
SELECT p.id, u.id, 5, 'AirPods Pro 2 chống ồn rất tốt, âm thanh trong trẻo. Dùng cả ngày không đau tai.', 
  'APPROVED', DATE_SUB(NOW(), INTERVAL 10 DAY)
FROM product p, app_user u
WHERE p.slug = 'apple-airpods-pro-2-usb-c' AND u.email = 'client1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id AND r.comment LIKE '%chống ồn rất tốt%');

INSERT INTO review (product_id, user_id, rating, comment, images, status, created_at)
SELECT p.id, u.id, 5, 'Nike AF1 kinh điển không bao giờ lỗi mốt. Đi êm chân, phối đồ dễ dàng.', 
  '["https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400"]', 'APPROVED', DATE_SUB(NOW(), INTERVAL 15 DAY)
FROM product p, app_user u
WHERE p.slug = 'nike-air-force-1-07-triple-white' AND u.email = 'client2@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id AND r.comment LIKE '%Nike AF1%');

INSERT INTO review (product_id, user_id, rating, comment, status, created_at)
SELECT p.id, u.id, 5, 'MacBook Pro M3 Pro xử lý video 4K mượt mà. Không hề nóng, yên tĩnh. Worth every penny!', 
  'APPROVED', DATE_SUB(NOW(), INTERVAL 8 DAY)
FROM product p, app_user u
WHERE p.slug = 'macbook-pro-14-m3-pro-512gb' AND u.email = 'client1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id AND r.comment LIKE '%MacBook Pro M3%');

INSERT INTO review (product_id, user_id, rating, comment, status, created_at)
SELECT p.id, u.id, 5, 'ROG Strix G16 chơi game đỉnh lắm! FPS cao, nhiệt độ ổn định. Tản nhiệt hoạt động tốt.', 
  'APPROVED', DATE_SUB(NOW(), INTERVAL 12 DAY)
FROM product p, app_user u
WHERE p.slug = 'asus-rog-strix-g16-i9-rtx4070' AND u.email = 'client2@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id AND r.comment LIKE '%ROG Strix%');

-- =============================================================================
-- 13. TẠO VOUCHER CHO SHOPS (ĐÃ SỬA ĐÚNG CỘT)
-- =============================================================================

INSERT INTO seller_voucher (shop_id, code, name, description, discount_type, discount_value, min_order_amount, 
  max_discount_amount, usage_limit, usage_count, usage_limit_per_user, start_date, end_date, status, created_at, updated_at)
SELECT s.id, 'APPLE20', 'Giảm 20% Apple Store', 'Giảm 20% tối đa 2 triệu cho đơn từ 10 triệu', 
  'PERCENTAGE', 20, 10000000, 2000000, 100, 0, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', NOW(), NOW()
FROM seller_shop s WHERE s.shop_name = 'Apple Store Official'
  AND NOT EXISTS (SELECT 1 FROM seller_voucher v WHERE v.shop_id = s.id AND v.code = 'APPLE20');

INSERT INTO seller_voucher (shop_id, code, name, description, discount_type, discount_value, min_order_amount, 
  max_discount_amount, usage_limit, usage_count, usage_limit_per_user, start_date, end_date, status, created_at, updated_at)
SELECT s.id, 'SAMSUNG15', 'Giảm 15% Samsung Hub', 'Giảm 15% tối đa 1.5 triệu', 
  'PERCENTAGE', 15, 5000000, 1500000, 200, 0, 2, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', NOW(), NOW()
FROM seller_shop s WHERE s.shop_name = 'Samsung Hub'
  AND NOT EXISTS (SELECT 1 FROM seller_voucher v WHERE v.shop_id = s.id AND v.code = 'SAMSUNG15');

INSERT INTO seller_voucher (shop_id, code, name, description, discount_type, discount_value, min_order_amount, 
  max_discount_amount, usage_limit, usage_count, usage_limit_per_user, start_date, end_date, status, created_at, updated_at)
SELECT s.id, 'XIAOMI500K', 'Giảm 500K Xiaomi', 'Giảm 500K cho đơn từ 5 triệu', 
  'FIXED_AMOUNT', 500000, 5000000, NULL, 150, 0, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', NOW(), NOW()
FROM seller_shop s WHERE s.shop_name = 'Xiaomi Official Store'
  AND NOT EXISTS (SELECT 1 FROM seller_voucher v WHERE v.shop_id = s.id AND v.code = 'XIAOMI500K');

INSERT INTO seller_voucher (shop_id, code, name, description, discount_type, discount_value, min_order_amount, 
  max_discount_amount, usage_limit, usage_count, usage_limit_per_user, start_date, end_date, status, created_at, updated_at)
SELECT s.id, 'FASHION10', 'Giảm 10% Thời trang', 'Giảm 10% cho tất cả sản phẩm', 
  'PERCENTAGE', 10, 500000, 500000, 500, 0, 3, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', NOW(), NOW()
FROM seller_shop s WHERE s.shop_name = 'Fashion Hub Vietnam'
  AND NOT EXISTS (SELECT 1 FROM seller_voucher v WHERE v.shop_id = s.id AND v.code = 'FASHION10');

INSERT INTO seller_voucher (shop_id, code, name, description, discount_type, discount_value, min_order_amount, 
  max_discount_amount, usage_limit, usage_count, usage_limit_per_user, start_date, end_date, status, created_at, updated_at)
SELECT s.id, 'TECH1M', 'Giảm 1 triệu Tech World', 'Giảm 1 triệu cho đơn từ 20 triệu', 
  'FIXED_AMOUNT', 1000000, 20000000, NULL, 50, 0, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', NOW(), NOW()
FROM seller_shop s WHERE s.shop_name = 'Tech World Store'
  AND NOT EXISTS (SELECT 1 FROM seller_voucher v WHERE v.shop_id = s.id AND v.code = 'TECH1M');

-- =============================================================================
-- 14. CẬP NHẬT REVIEW COUNT VÀ RATING CHO PRODUCTS
-- =============================================================================

UPDATE product p
SET review_count = (SELECT COUNT(*) FROM review r WHERE r.product_id = p.id AND r.status = 'APPROVED'),
    average_rating = COALESCE((SELECT AVG(r.rating) FROM review r WHERE r.product_id = p.id AND r.status = 'APPROVED'), 0),
    updated_at = NOW()
WHERE EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id);

SET FOREIGN_KEY_CHECKS=1;

-- =============================================================================
-- HOÀN THÀNH SEED DATA
-- =============================================================================
