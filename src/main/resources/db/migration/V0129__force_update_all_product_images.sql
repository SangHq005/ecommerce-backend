-- =============================================================================
-- Migration: Update Individual Product Images with 4 Color Variants
-- Description: Cập nhật hình ảnh cho từng sản phẩm cụ thể, mỗi sản phẩm có 4 ảnh khác nhau về màu
-- =============================================================================

-- Xóa tất cả product_image cũ trước khi insert lại
DELETE pi FROM product_image pi
INNER JOIN product p ON p.id = pi.product_id
WHERE p.status = 'ACTIVE';

-- =============================================================================
-- IPHONE 15 PRO MAX - 4 màu khác nhau
-- =============================================================================

-- iPhone 15 Pro Max (Titan)
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070795/8ba558e80bb840e0d22ec086ece78410_tbvs83.jpg'
WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072164/370dcb99f3616ed1725c311d3eddc11b_mdr298.jpg', 1, NOW() FROM product WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072191/4271e26010461fa35db43855414ed6bb_xehwgp.jpg', 2, NOW() FROM product WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072217/4dcf097b9836a776bcba087e08461672_akslt2.jpg', 3, NOW() FROM product WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769069060/1884248df0286062436ea23d29ef5183-removebg-preview_jnqx4t.png', 4, NOW() FROM product WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE';

-- iPhone 15 Pro Max 256GB
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072252/3947d214dabd11bd7950fbdc66dc9f67_u2jzgy.jpg',
    updated_at = NOW()
WHERE slug = 'iphone-15-pro-max-256gb-chinh-hang' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072277/152fc4f7bf5cc9ef6586f1e476dd9744_obzruc.jpg', 1, NOW() FROM product WHERE slug = 'iphone-15-pro-max-256gb-chinh-hang' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070795/8ba558e80bb840e0d22ec086ece78410_tbvs83.jpg', 2, NOW() FROM product WHERE slug = 'iphone-15-pro-max-256gb-chinh-hang' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072164/370dcb99f3616ed1725c311d3eddc11b_mdr298.jpg', 3, NOW() FROM product WHERE slug = 'iphone-15-pro-max-256gb-chinh-hang' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072191/4271e26010461fa35db43855414ed6bb_xehwgp.jpg', 4, NOW() FROM product WHERE slug = 'iphone-15-pro-max-256gb-chinh-hang' AND status = 'ACTIVE';

-- =============================================================================
-- SAMSUNG GALAXY S24 ULTRA - 4 màu khác nhau
-- =============================================================================

-- Samsung Galaxy S24 Ultra
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070835/2f4543e8b063f32c4346ace156929ceb_crht0l.jpg',
    updated_at = NOW()
WHERE slug = 'samsung-galaxy-s24-ultra' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070835/2f4543e8b063f32c4346ace156929ceb_crht0l.jpg', 1, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070240/1a60143e505f9a07d8138200bec03dde_sl6zsq.jpg', 2, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072423/81767d9061a372f581106e34778f9ca8_i9dn2n.jpg', 3, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80', 4, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra' AND status = 'ACTIVE';

-- Samsung Galaxy S24 Ultra 256GB
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072423/81767d9061a372f581106e34778f9ca8_i9dn2n.jpg',
    updated_at = NOW()
WHERE slug = 'samsung-galaxy-s24-ultra-256gb' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072423/81767d9061a372f581106e34778f9ca8_i9dn2n.jpg', 1, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070835/2f4543e8b063f32c4346ace156929ceb_crht0l.jpg', 2, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070240/1a60143e505f9a07d8138200bec03dde_sl6zsq.jpg', 3, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&q=80', 4, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra-256gb' AND status = 'ACTIVE';

-- =============================================================================
-- XIAOMI 14 ULTRA - 4 màu khác nhau
-- =============================================================================

UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072497/1463218895245e34079ccb977ef4f3fc_uqr5dy.jpg',
    updated_at = NOW()
WHERE slug = 'xiaomi-14-ultra-512gb-leica' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072497/1463218895245e34079ccb977ef4f3fc_uqr5dy.jpg', 1, NOW() FROM product WHERE slug = 'xiaomi-14-ultra-512gb-leica' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070925/86269ac3bf4f93b11581c15d653637a9_bi0zjp.jpg', 2, NOW() FROM product WHERE slug = 'xiaomi-14-ultra-512gb-leica' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80', 3, NOW() FROM product WHERE slug = 'xiaomi-14-ultra-512gb-leica' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&q=80', 4, NOW() FROM product WHERE slug = 'xiaomi-14-ultra-512gb-leica' AND status = 'ACTIVE';

-- =============================================================================
-- NIKE AIR JORDAN - 4 màu khác nhau
-- =============================================================================

UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072546/d825d1b89c90bfc78519eb0b74582ec1_mu9c0o.jpg',
    updated_at = NOW()
WHERE slug = 'nike-air-jordan-1-retro-high' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072546/d825d1b89c90bfc78519eb0b74582ec1_mu9c0o.jpg', 1, NOW() FROM product WHERE slug = 'nike-air-jordan-1-retro-high' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070300/f6efc57face2dce9a35e60e3f82c83db_tjbhsa.jpg', 2, NOW() FROM product WHERE slug = 'nike-air-jordan-1-retro-high' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', 3, NOW() FROM product WHERE slug = 'nike-air-jordan-1-retro-high' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&q=80', 4, NOW() FROM product WHERE slug = 'nike-air-jordan-1-retro-high' AND status = 'ACTIVE';

-- =============================================================================
-- CÁC SẢN PHẨM KHÁC - Tự động tạo 4 ảnh cho tất cả products còn lại
-- =============================================================================

-- Update main_image_url cho các products chưa được update ở trên
UPDATE product p
SET p.main_image_url = CASE
    -- Điện thoại/Smartphone
    WHEN p.category_id IN (SELECT id FROM category WHERE slug LIKE '%dien-thoai%' OR slug LIKE '%smartphone%') THEN
        'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80'
    -- Laptop
    WHEN p.category_id IN (SELECT id FROM category WHERE slug LIKE '%laptop%' OR slug LIKE '%may-tinh%') THEN
        'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80'
    -- Giày dép
    WHEN p.category_id IN (SELECT id FROM category WHERE slug LIKE '%giay%' OR slug LIKE '%dep%' OR slug LIKE '%sneaker%') THEN
        'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80'
    -- Quần áo/Thời trang
    WHEN p.category_id IN (SELECT id FROM category WHERE slug LIKE '%quan-ao%' OR slug LIKE '%thoi-trang%' OR slug LIKE '%ao%' OR slug LIKE '%quan%') THEN
        'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&q=80'
    -- Phụ kiện công nghệ
    WHEN p.category_id IN (SELECT id FROM category WHERE slug LIKE '%phu-kien%' OR slug LIKE '%tai-nghe%' OR slug LIKE '%sac%') THEN
        'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80'
    -- Mặc định
    ELSE 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80'
END,
p.updated_at = NOW()
WHERE p.status = 'ACTIVE'
  AND p.slug NOT IN (
    'iphone-15-pro-max-titan',
    'iphone-15-pro-max-256gb-chinh-hang',
    'samsung-galaxy-s24-ultra',
    'samsung-galaxy-s24-ultra-256gb',
    'xiaomi-14-ultra-512gb-leica',
    'nike-air-jordan-1-retro-high'
  );

-- Insert 4 ảnh cho tất cả products còn lại (theo category)
-- Điện thoại/Smartphone
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80', 1, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%dien-thoai%' OR slug LIKE '%smartphone%')
  AND p.slug NOT IN (
    'iphone-15-pro-max-titan',
    'iphone-15-pro-max-256gb-chinh-hang',
    'samsung-galaxy-s24-ultra',
    'samsung-galaxy-s24-ultra-256gb',
    'xiaomi-14-ultra-512gb-leica'
  )
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&q=80', 2, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%dien-thoai%' OR slug LIKE '%smartphone%')
  AND p.slug NOT IN (
    'iphone-15-pro-max-titan',
    'iphone-15-pro-max-256gb-chinh-hang',
    'samsung-galaxy-s24-ultra',
    'samsung-galaxy-s24-ultra-256gb',
    'xiaomi-14-ultra-512gb-leica'
  )
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=800&q=80', 3, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%dien-thoai%' OR slug LIKE '%smartphone%')
  AND p.slug NOT IN (
    'iphone-15-pro-max-titan',
    'iphone-15-pro-max-256gb-chinh-hang',
    'samsung-galaxy-s24-ultra',
    'samsung-galaxy-s24-ultra-256gb',
    'xiaomi-14-ultra-512gb-leica'
  )
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=800&q=80', 4, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%dien-thoai%' OR slug LIKE '%smartphone%')
  AND p.slug NOT IN (
    'iphone-15-pro-max-titan',
    'iphone-15-pro-max-256gb-chinh-hang',
    'samsung-galaxy-s24-ultra',
    'samsung-galaxy-s24-ultra-256gb',
    'xiaomi-14-ultra-512gb-leica'
  )
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 4);

-- Laptop
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80', 1, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%laptop%' OR slug LIKE '%may-tinh%')
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&q=80', 2, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%laptop%' OR slug LIKE '%may-tinh%')
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=800&q=80', 3, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%laptop%' OR slug LIKE '%may-tinh%')
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=800&q=80', 4, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%laptop%' OR slug LIKE '%may-tinh%')
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 4);

-- Giày dép
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80', 1, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%giay%' OR slug LIKE '%dep%' OR slug LIKE '%sneaker%')
  AND p.slug != 'nike-air-jordan-1-retro-high'
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&q=80', 2, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%giay%' OR slug LIKE '%dep%' OR slug LIKE '%sneaker%')
  AND p.slug != 'nike-air-jordan-1-retro-high'
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=800&q=80', 3, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%giay%' OR slug LIKE '%dep%' OR slug LIKE '%sneaker%')
  AND p.slug != 'nike-air-jordan-1-retro-high'
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=800&q=80', 4, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%giay%' OR slug LIKE '%dep%' OR slug LIKE '%sneaker%')
  AND p.slug != 'nike-air-jordan-1-retro-high'
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 4);

-- Quần áo/Thời trang
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&q=80', 1, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%quan-ao%' OR slug LIKE '%thoi-trang%' OR slug LIKE '%ao%' OR slug LIKE '%quan%')
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&q=80', 2, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%quan-ao%' OR slug LIKE '%thoi-trang%' OR slug LIKE '%ao%' OR slug LIKE '%quan%')
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1552374196-c4e7ffc6e126?w=800&q=80', 3, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%quan-ao%' OR slug LIKE '%thoi-trang%' OR slug LIKE '%ao%' OR slug LIKE '%quan%')
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=800&q=80', 4, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%quan-ao%' OR slug LIKE '%thoi-trang%' OR slug LIKE '%ao%' OR slug LIKE '%quan%')
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 4);

-- Phụ kiện công nghệ
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769068925/69427fad3984e11c6c6071b5494df301-removebg-preview_ixksfm.png', 1, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%phu-kien%' OR slug LIKE '%tai-nghe%' OR slug LIKE '%sac%')
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1484704849700-f032a568e944?w=800&q=80', 2, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%phu-kien%' OR slug LIKE '%tai-nghe%' OR slug LIKE '%sac%')
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 1)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1572569511254-d8f925fe2cbb?w=800&q=80', 3, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%phu-kien%' OR slug LIKE '%tai-nghe%' OR slug LIKE '%sac%')
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 2)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, 'https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?w=800&q=80', 4, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.category_id IN (SELECT id FROM category WHERE slug LIKE '%phu-kien%' OR slug LIKE '%tai-nghe%' OR slug LIKE '%sac%')
  AND EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 3)
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id AND pi.sort_order = 4);

-- Mặc định cho các products khác
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, p.main_image_url, 1, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.main_image_url IS NOT NULL
  AND p.main_image_url != ''
  AND p.main_image_url LIKE 'http%'
  AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id = p.id);
