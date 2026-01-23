-- ============================================================================
-- V0138: Fix Duplicate Product Images
-- Update products that have duplicate or incorrect images with proper product images
-- ============================================================================

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================================
-- PART 1: RUNNING SHOES PRO - Update with proper running shoes image
-- ============================================================================

UPDATE product
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769106543/ba4f246bbb9d7978ff4484e5013b5d8b_pi3sdp.jpg'
WHERE (slug = 'running-shoes-pro' OR name LIKE '%Running Shoes Pro%')
  AND status = 'ACTIVE';

-- Delete old product images and add new ones
DELETE FROM product_image
WHERE product_id IN (
  SELECT id FROM product WHERE (slug = 'running-shoes-pro' OR name LIKE '%Running Shoes Pro%') AND status = 'ACTIVE'
);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop&q=80',
  1,
  NOW()
FROM product
WHERE (slug = 'running-shoes-pro' OR name LIKE '%Running Shoes Pro%') AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.image_url = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop&q=80'
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&h=800&fit=crop&q=80',
  2,
  NOW()
FROM product
WHERE (slug = 'running-shoes-pro' OR name LIKE '%Running Shoes Pro%') AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.image_url = 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&h=800&fit=crop&q=80'
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800&h=800&fit=crop&q=80',
  3,
  NOW()
FROM product
WHERE (slug = 'running-shoes-pro' OR name LIKE '%Running Shoes Pro%') AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.image_url = 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800&h=800&fit=crop&q=80'
  );

-- ============================================================================
-- PART 2: NIKE AIR MAX 270 - Update with proper Nike shoes image
-- ============================================================================

UPDATE product
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074813/a0bbfcf435aef827455be127f948a758_jhue4y.jpg'
WHERE slug = 'nike-air-max-270' AND status = 'ACTIVE'
  AND main_image_url != 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074813/a0bbfcf435aef827455be127f948a758_jhue4y.jpg';

-- Delete old product images and add new ones
DELETE FROM product_image
WHERE product_id IN (
  SELECT id FROM product WHERE slug = 'nike-air-max-270' AND status = 'ACTIVE'
);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074813/a0bbfcf435aef827455be127f948a758_jhue4y.jpg',
  1,
  NOW()
FROM product
WHERE slug = 'nike-air-max-270' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 1
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074811/e1b6f4d3c856dd7c5f5bdc9c1b2e51ad_zut0uw.jpg',
  2,
  NOW()
FROM product
WHERE slug = 'nike-air-max-270' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 2
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop&q=80',
  3,
  NOW()
FROM product
WHERE slug = 'nike-air-max-270' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 3
  );

-- ============================================================================
-- PART 3: ZARA MEN SLIM FIT SUIT - Update with proper suit image
-- ============================================================================

UPDATE product
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769106618/aed4eff5ce05a9fcbc9c5c90d07134b2_xirinz.jpg'
WHERE slug = 'zara-men-slim-fit-suit' AND status = 'ACTIVE'
  AND main_image_url != 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&h=800&fit=crop&q=80';

-- Delete old product images and add new ones
DELETE FROM product_image
WHERE product_id IN (
  SELECT id FROM product WHERE slug = 'zara-men-slim-fit-suit' AND status = 'ACTIVE'
);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&h=800&fit=crop&q=80',
  1,
  NOW()
FROM product
WHERE slug = 'zara-men-slim-fit-suit' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 1
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=800&fit=crop&q=80',
  2,
  NOW()
FROM product
WHERE slug = 'zara-men-slim-fit-suit' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 2
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1617137968427-85924c600a20?w=800&h=800&fit=crop&q=80',
  3,
  NOW()
FROM product
WHERE slug = 'zara-men-slim-fit-suit' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 3
  );

-- ============================================================================
-- PART 4: DELL XPS 15 - Update with proper laptop image
-- ============================================================================

UPDATE product
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074393/94a6a956eb97045f96d7ef7aae280ec6_zsdai8.jpg'
WHERE (slug = 'dell-xps-15' OR slug = 'dell-xps-15-9530-i7-rtx4060') AND status = 'ACTIVE'
  AND main_image_url != 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074393/94a6a956eb97045f96d7ef7aae280ec6_zsdai8.jpg';

-- Delete old product images and add new ones for dell-xps-15 (old slug)
DELETE FROM product_image
WHERE product_id IN (
  SELECT id FROM product WHERE slug = 'dell-xps-15' AND status = 'ACTIVE'
);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074393/94a6a956eb97045f96d7ef7aae280ec6_zsdai8.jpg',
  1,
  NOW()
FROM product
WHERE slug = 'dell-xps-15' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 1
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800&h=800&fit=crop&q=80',
  2,
  NOW()
FROM product
WHERE slug = 'dell-xps-15' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 2
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&h=800&fit=crop&q=80',
  3,
  NOW()
FROM product
WHERE slug = 'dell-xps-15' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 3
  );

-- ============================================================================
-- PART 5: MACBOOK AIR 15 M3 - Update with proper MacBook image
-- ============================================================================

UPDATE product
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074551/91e9d7f4d51729dee6f7210c663f5bc4_ot6fiq.jpg'
WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE'
  AND main_image_url != 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074551/91e9d7f4d51729dee6f7210c663f5bc4_ot6fiq.jpg';

-- Delete old product images and add new ones
DELETE FROM product_image
WHERE product_id IN (
  SELECT id FROM product WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE'
);

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074551/91e9d7f4d51729dee6f7210c663f5bc4_ot6fiq.jpg',
  1,
  NOW()
FROM product
WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 1
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074549/68ca6326e6fbb0fca322cf07305d8cbe_ldbrxd.jpg',
  2,
  NOW()
FROM product
WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 2
  );

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT 
  id,
  'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&h=800&fit=crop&q=80',
  3,
  NOW()
FROM product
WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM product_image pi 
    WHERE pi.product_id = product.id AND pi.sort_order = 3
  );

-- ============================================================================
-- PART 6: FIX PRODUCTS WITH HEADPHONE IMAGES (should be other products)
-- ============================================================================

-- Find products that might have wrong images (headphones image for non-headphone products)
-- Update any product that has placeholder or wrong images

-- Update products with generic placeholder images to proper product images
UPDATE product p
SET p.main_image_url = CASE
  -- If it's a shoe product but has headphone image, update to shoe image
  WHEN p.slug LIKE '%shoe%' OR p.slug LIKE '%giay%' OR p.name LIKE '%Shoe%' OR p.name LIKE '%Giày%' THEN
    'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop&q=80'
  -- If it's a clothing product but has wrong image, update to clothing image
  WHEN p.slug LIKE '%suit%' OR p.slug LIKE '%ao%' OR p.slug LIKE '%quan%' OR p.name LIKE '%Suit%' OR p.name LIKE '%Áo%' OR p.name LIKE '%Quần%' THEN
    'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&h=800&fit=crop&q=80'
  -- If it's a laptop product but has wrong image, update to laptop image
  WHEN p.slug LIKE '%laptop%' OR p.slug LIKE '%macbook%' OR p.slug LIKE '%dell%' OR p.name LIKE '%Laptop%' OR p.name LIKE '%MacBook%' OR p.name LIKE '%Dell%' THEN
    'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800&h=800&fit=crop&q=80'
  ELSE p.main_image_url
END
WHERE p.status = 'ACTIVE'
  AND (
    p.main_image_url LIKE '%placeholder%' 
    OR p.main_image_url LIKE '%via.placeholder%'
    OR p.main_image_url LIKE '%headphone%'
    OR p.main_image_url LIKE '%1546435770%' -- Common headphone image ID
  );

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================================
-- END OF DUPLICATE IMAGES FIX MIGRATION
-- ============================================================================
