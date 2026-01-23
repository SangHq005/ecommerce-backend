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

