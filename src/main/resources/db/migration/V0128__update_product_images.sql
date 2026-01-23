-- =============================================================================
-- Migration: Update Product Images
-- Description: Cập nhật hình ảnh cho tất cả sản phẩm
-- =============================================================================

-- 1. Cập nhật main_image_url cho các products chưa có hoặc có URL rỗng
UPDATE product p
SET p.main_image_url = CASE
    -- Điện thoại/Smartphone
    WHEN p.category_id IN (SELECT id FROM category WHERE slug LIKE '%dien-thoai%' OR slug LIKE '%smartphone%') THEN
        CASE 
            WHEN p.name LIKE '%iPhone%' OR p.name LIKE '%iphone%' THEN 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80'
            WHEN p.name LIKE '%Samsung%' OR p.name LIKE '%Galaxy%' THEN 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80'
            WHEN p.name LIKE '%Xiaomi%' THEN 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80'
            WHEN p.name LIKE '%Oppo%' THEN 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80'
            ELSE 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80'
        END
    -- Laptop
    WHEN p.category_id IN (SELECT id FROM category WHERE slug LIKE '%laptop%' OR slug LIKE '%may-tinh%') THEN
        CASE
            WHEN p.name LIKE '%MacBook%' OR p.name LIKE '%macbook%' THEN 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80'
            WHEN p.name LIKE '%Dell%' THEN 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&q=80'
            WHEN p.name LIKE '%HP%' THEN 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&q=80'
            WHEN p.name LIKE '%Lenovo%' THEN 'https://images.unsplash.com/photo-1496181133206-80ceb88a853?w=800&q=80'
            ELSE 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&q=80'
        END
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
  AND (p.main_image_url IS NULL OR p.main_image_url = '' OR p.main_image_url NOT LIKE 'http%');

-- 2. Xóa các product_image cũ không hợp lệ
DELETE FROM product_image 
WHERE image_url IS NULL 
   OR image_url = '' 
   OR image_url NOT LIKE 'http%';

-- 3. Thêm product_image từ main_image_url cho các products chưa có image
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, p.main_image_url, 1, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.main_image_url IS NOT NULL
  AND p.main_image_url != ''
  AND p.main_image_url LIKE 'http%'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = p.id AND pi.sort_order = 1
  );

-- 4. Thêm thêm 2-3 hình ảnh phụ cho các products có variant (để hiển thị các màu khác nhau)
-- Thêm image thứ 2
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
    p.id,
    CASE 
        -- iPhone - các màu khác nhau
        WHEN p.name LIKE '%iPhone%' OR p.name LIKE '%iphone%' THEN
            CASE (p.id % 4)
                WHEN 0 THEN 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80'
                WHEN 1 THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&q=80'
                WHEN 2 THEN 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=800&q=80'
                ELSE 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80'
            END
        -- Samsung
        WHEN p.name LIKE '%Samsung%' OR p.name LIKE '%Galaxy%' THEN
            CASE (p.id % 3)
                WHEN 0 THEN 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&q=80'
                WHEN 1 THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&q=80'
                ELSE 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=800&q=80'
            END
        -- Giày
        WHEN p.category_id IN (SELECT id FROM category WHERE slug LIKE '%giay%' OR slug LIKE '%sneaker%') THEN
            CASE (p.id % 3)
                WHEN 0 THEN 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80'
                WHEN 1 THEN 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&q=80'
                ELSE 'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=800&q=80'
            END
        -- Mặc định
        ELSE 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80'
    END AS image_url,
    2 AS sort_order,
    NOW() AS created_at
FROM product p
WHERE p.status = 'ACTIVE'
  AND EXISTS (
    SELECT 1 FROM product_option_group pog WHERE pog.product_id = p.id
  )
  AND (SELECT COUNT(*) FROM product_image pi WHERE pi.product_id = p.id) = 1
LIMIT 100;

-- Thêm image thứ 3 cho một số products
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
    p.id,
    CASE 
        WHEN p.name LIKE '%iPhone%' OR p.name LIKE '%iphone%' THEN 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=800&q=80'
        WHEN p.name LIKE '%Samsung%' OR p.name LIKE '%Galaxy%' THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&q=80'
        WHEN p.category_id IN (SELECT id FROM category WHERE slug LIKE '%giay%' OR slug LIKE '%sneaker%') THEN 'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=800&q=80'
        ELSE 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80'
    END AS image_url,
    3 AS sort_order,
    NOW() AS created_at
FROM product p
WHERE p.status = 'ACTIVE'
  AND EXISTS (
    SELECT 1 FROM product_option_group pog WHERE pog.product_id = p.id
  )
  AND (SELECT COUNT(*) FROM product_image pi WHERE pi.product_id = p.id) = 2
LIMIT 50;

-- 5. Cập nhật product_image cho các products đã có SKU với image_url
UPDATE product_image pi
INNER JOIN product_sku ps ON ps.product_id = pi.product_id
SET pi.image_url = ps.image_url
WHERE ps.image_url IS NOT NULL 
  AND ps.image_url != ''
  AND ps.image_url LIKE 'http%'
  AND pi.sort_order = 1
  AND (pi.image_url IS NULL OR pi.image_url = '' OR pi.image_url NOT LIKE 'http%');

-- 6. Đảm bảo mỗi product có ít nhất 1 image (nếu chưa có)
INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT p.id, p.main_image_url, 1, NOW()
FROM product p
WHERE p.status = 'ACTIVE'
  AND p.main_image_url IS NOT NULL
  AND p.main_image_url != ''
  AND p.main_image_url LIKE 'http%'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi WHERE pi.product_id = p.id
  );
