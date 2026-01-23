-- ============================================================================
-- V0121: Create Products with Variants Support
-- Create products with correct slugs that match V0116 variant definitions
-- ============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================================
-- PART 1: CREATE IPHONE 15 PRO MAX (if not exists)
-- ============================================================================

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'iPhone 15 Pro Max - Chính hãng VN/A',
  'iphone-15-pro-max-titan',
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
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769069060/1884248df0286062436ea23d29ef5183-removebg-preview_jnqx4t.png',
  34990000, 36990000, 0, 'VND', 'ACTIVE', 4.9, 256, 1250, 221, TRUE, 95, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'iphone-15-pro-max-titan' AND p.shop_id = s.id)
LIMIT 1;

-- ============================================================================
-- PART 2: CREATE SAMSUNG GALAXY S24 ULTRA (if not exists)
-- ============================================================================

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Samsung Galaxy S24 Ultra - Chính hãng',
  'samsung-galaxy-s24-ultra',
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
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070240/1a60143e505f9a07d8138200bec03dde_sl6zsq.jpg',
  31990000, 33990000, 0, 'VND', 'ACTIVE', 4.8, 189, 980, 233, TRUE, 92, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Samsung Hub' AND c.slug = 'dien-thoai-smartphone' AND b.slug = 'samsung'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'samsung-galaxy-s24-ultra' AND p.shop_id = s.id)
LIMIT 1;

-- ============================================================================
-- PART 3: CREATE NIKE AIR JORDAN (if not exists)
-- ============================================================================

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, original_price, stock_quantity, currency, status, average_rating, review_count, sold_count, weight_grams, is_featured, quality_score, published_at, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id,
  'Nike Air Jordan 1 Retro High OG',
  'nike-air-jordan-1-retro-high',
  '<h2>Nike Air Jordan 1 Retro High OG - Huyền thoại trở lại</h2>
<p>Air Jordan 1 Retro High là đôi giày huyền thoại được tái phát hành với thiết kế nguyên bản năm 1985, chất liệu da cao cấp và màu sắc cổ điển.</p>
<h3>Điểm nổi bật:</h3>
<ul>
<li><strong>Thiết kế nguyên bản:</strong> Giữ nguyên form dáng năm 1985</li>
<li><strong>Chất liệu da cao cấp:</strong> Da thật, bền đẹp</li>
<li><strong>Màu sắc cổ điển:</strong> Chicago, Black/White, Blue/White</li>
<li><strong>Logo Wings:</strong> Logo Wings nguyên bản trên cổ giày</li>
<li><strong>Đế cao su:</strong> Bền, chống trượt tốt</li>
</ul>',
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070300/f6efc57face2dce9a35e60e3f82c83db_tjbhsa.jpg',
  3500000, 3800000, 0, 'VND', 'ACTIVE', 4.7, 342, 890, 450, TRUE, 88, NOW(), NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Sport Fashion Store' AND c.slug = 'giay-the-thao' AND b.slug = 'nike'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.slug = 'nike-air-jordan-1-retro-high' AND p.shop_id = s.id)
LIMIT 1;

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================================
-- END OF PRODUCT CREATION MIGRATION
-- ============================================================================
