-- =============================================================================
-- Migration: Fix Image URL Typos
-- Description: Sửa các lỗi typo trong URL hình ảnh (hhttps:// -> https://, .jpg0 -> .jpg)
-- =============================================================================

-- Sửa lỗi typo hhttps:// thành https:// trong product.main_image_url
UPDATE product 
SET main_image_url = REPLACE(main_image_url, 'hhttps://', 'https://'),
    updated_at = NOW()
WHERE main_image_url LIKE 'hhttps://%';

-- Sửa lỗi typo hhttps:// thành https:// trong product_image.image_url
UPDATE product_image 
SET image_url = REPLACE(image_url, 'hhttps://', 'https://')
WHERE image_url LIKE 'hhttps://%';

-- Sửa lỗi typo .jpg0 thành .jpg trong product.main_image_url
UPDATE product 
SET main_image_url = REPLACE(main_image_url, '.jpg0', '.jpg'),
    updated_at = NOW()
WHERE main_image_url LIKE '%.jpg0';

-- Sửa lỗi typo .jpg0 thành .jpg trong product_image.image_url
UPDATE product_image 
SET image_url = REPLACE(image_url, '.jpg0', '.jpg')
WHERE image_url LIKE '%.jpg0';
