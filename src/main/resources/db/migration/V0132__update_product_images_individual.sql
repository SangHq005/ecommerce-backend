-- =============================================================================
-- Migration: Update Product Images - Individual Products
-- Description: Cập nhật hình ảnh cho từng sản phẩm riêng biệt
--              Mỗi sản phẩm có 4 ảnh khác nhau về màu
--              Dễ dàng chỉnh sửa từng sản phẩm
-- =============================================================================

-- Xóa product_image cũ của các sản phẩm được cập nhật
DELETE pi FROM product_image pi
INNER JOIN product p ON p.id = pi.product_id
WHERE p.status = 'ACTIVE'
  AND p.slug IN (
    'iphone-15-pro-max-titan',
    'iphone-15-pro-max-256gb-chinh-hang',
    'iphone-15-128gb-chinh-hang',
    'samsung-galaxy-s24-ultra',
    'samsung-galaxy-s24-ultra-256gb',
    'samsung-galaxy-a55-5g-128gb',
    'xiaomi-14-ultra-512gb-leica',
    'xiaomi-redmi-note-13-pro-plus-5g',
    'oppo-find-x7-ultra-256gb',
    'nike-air-jordan-1-retro-high',
    'macbook-pro-14-m3-pro-512gb',
    'dell-xps-15-9530-i7-rtx4060',
    'asus-rog-strix-g16-i9-rtx4070',
    'lenovo-thinkpad-x1-carbon-gen11',
    'macbook-air-15-m3-256gb',
    'apple-airpods-pro-2-usb-c',
    'samsung-galaxy-buds3-pro',
    'apple-watch-series-9-gps-45mm',
    'samsung-galaxy-watch-6-classic-47mm',
    'nike-air-force-1-07-triple-white',
    'adidas-ultraboost-23-core-black',
    'ao-polo-uniqlo-dry-ex-navy'
  );

-- =============================================================================
-- 1. IPHONE 15 PRO MAX (TITAN)
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070795/8ba558e80bb840e0d22ec086ece78410_tbvs83.jpg',
    updated_at = NOW()
WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072164/370dcb99f3616ed1725c311d3eddc11b_mdr298.jpg', 1, NOW() FROM product WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072191/4271e26010461fa35db43855414ed6bb_xehwgp.jpg', 2, NOW() FROM product WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072217/4dcf097b9836a776bcba087e08461672_akslt2.jpg', 3, NOW() FROM product WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769069060/1884248df0286062436ea23d29ef5183-removebg-preview_jnqx4t.png', 4, NOW() FROM product WHERE slug = 'iphone-15-pro-max-titan' AND status = 'ACTIVE';

-- =============================================================================
-- 2. IPHONE 15 PRO MAX 256GB
-- =============================================================================
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
-- 3. IPHONE 15 128GB
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071143/22119c436265bb773a93950e73970a5e_jtnfvy.jpg',
    updated_at = NOW()
WHERE slug = 'iphone-15-128gb-chinh-hang' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071143/22119c436265bb773a93950e73970a5e_jtnfvy.jpg', 1, NOW() FROM product WHERE slug = 'iphone-15-128gb-chinh-hang' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074120/f4e65a02deac3a739cbb6cf4bee249b8_xha1xi.jpg', 2, NOW() FROM product WHERE slug = 'iphone-15-128gb-chinh-hang' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074120/f4e65a02deac3a739cbb6cf4bee249b8_xha1xi.jpg', 3, NOW() FROM product WHERE slug = 'iphone-15-128gb-chinh-hang' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074120/f4e65a02deac3a739cbb6cf4bee249b8_xha1xi.jpg', 4, NOW() FROM product WHERE slug = 'iphone-15-128gb-chinh-hang' AND status = 'ACTIVE';

-- =============================================================================
-- 4. SAMSUNG GALAXY S24 ULTRA
-- =============================================================================
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
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074120/f4e65a02deac3a739cbb6cf4bee249b8_xha1xi.jpg', 4, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra' AND status = 'ACTIVE';

-- =============================================================================
-- 5. SAMSUNG GALAXY S24 ULTRA 256GB
-- =============================================================================
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
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070240/1a60143e505f9a07d8138200bec03dde_sl6zsq.jpg', 4, NOW() FROM product WHERE slug = 'samsung-galaxy-s24-ultra-256gb' AND status = 'ACTIVE';

-- =============================================================================
-- 6. SAMSUNG GALAXY A55 5G
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071087/2ffb48558701e9b9449cb16d14e3de72_kfzphc.jpg',
    updated_at = NOW()
WHERE slug = 'samsung-galaxy-a55-5g-128gb' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071087/2ffb48558701e9b9449cb16d14e3de72_kfzphc.jpg', 1, NOW() FROM product WHERE slug = 'samsung-galaxy-a55-5g-128gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071087/2ffb48558701e9b9449cb16d14e3de72_kfzphc.jpg', 2, NOW() FROM product WHERE slug = 'samsung-galaxy-a55-5g-128gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071087/2ffb48558701e9b9449cb16d14e3de72_kfzphc.jpg', 3, NOW() FROM product WHERE slug = 'samsung-galaxy-a55-5g-128gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071087/2ffb48558701e9b9449cb16d14e3de72_kfzphc.jpg', 4, NOW() FROM product WHERE slug = 'samsung-galaxy-a55-5g-128gb' AND status = 'ACTIVE';

-- =============================================================================
-- 7. XIAOMI 14 ULTRA
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
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072497/1463218895245e34079ccb977ef4f3fc_uqr5dy.jpg', 3, NOW() FROM product WHERE slug = 'xiaomi-14-ultra-512gb-leica' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072497/1463218895245e34079ccb977ef4f3fc_uqr5dy.jpg', 4, NOW() FROM product WHERE slug = 'xiaomi-14-ultra-512gb-leica' AND status = 'ACTIVE';

-- =============================================================================
-- 8. XIAOMI REDMI NOTE 13 PRO+
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071051/2bbde907985112c55b1438d288b997d0_szboav.jpg',
    updated_at = NOW()
WHERE slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071051/2bbde907985112c55b1438d288b997d0_szboav.jpg', 1, NOW() FROM product WHERE slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071051/2bbde907985112c55b1438d288b997d0_szboav.jpg', 2, NOW() FROM product WHERE slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071051/2bbde907985112c55b1438d288b997d0_szboav.jpg', 3, NOW() FROM product WHERE slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071051/2bbde907985112c55b1438d288b997d0_szboav.jpg', 4, NOW() FROM product WHERE slug = 'xiaomi-redmi-note-13-pro-plus-5g' AND status = 'ACTIVE';

-- =============================================================================
-- 9. OPPO FIND X7 ULTRA
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071005/ff0091afa5382c9662a0e7a87f42a164_nmb7ci.jpg',
    updated_at = NOW()
WHERE slug = 'oppo-find-x7-ultra-256gb' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071005/ff0091afa5382c9662a0e7a87f42a164_nmb7ci.jpg', 1, NOW() FROM product WHERE slug = 'oppo-find-x7-ultra-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071005/ff0091afa5382c9662a0e7a87f42a164_nmb7ci.jpg', 2, NOW() FROM product WHERE slug = 'oppo-find-x7-ultra-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071005/ff0091afa5382c9662a0e7a87f42a164_nmb7ci.jpg', 3, NOW() FROM product WHERE slug = 'oppo-find-x7-ultra-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769071005/ff0091afa5382c9662a0e7a87f42a164_nmb7ci.jpg', 4, NOW() FROM product WHERE slug = 'oppo-find-x7-ultra-256gb' AND status = 'ACTIVE';

-- =============================================================================
-- 10. NIKE AIR JORDAN 1 RETRO HIGH
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
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072546/d825d1b89c90bfc78519eb0b74582ec1_mu9c0o.jpg', 3, NOW() FROM product WHERE slug = 'nike-air-jordan-1-retro-high' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769072546/d825d1b89c90bfc78519eb0b74582ec1_mu9c0o.jpg', 4, NOW() FROM product WHERE slug = 'nike-air-jordan-1-retro-high' AND status = 'ACTIVE';

-- =============================================================================
-- 11. MACBOOK PRO 14 M3 PRO
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074302/18307dfde0f655618d822607bda8c931_juyktb.jpg',
    updated_at = NOW()
WHERE slug = 'macbook-pro-14-m3-pro-512gb' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074335/eeb5276ef5a14928bcdf0e77c8584416_qgpgne.jpg', 1, NOW() FROM product WHERE slug = 'macbook-pro-14-m3-pro-512gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074302/18307dfde0f655618d822607bda8c931_juyktb.jpg', 2, NOW() FROM product WHERE slug = 'macbook-pro-14-m3-pro-512gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074335/eeb5276ef5a14928bcdf0e77c8584416_qgpgne.jpg', 3, NOW() FROM product WHERE slug = 'macbook-pro-14-m3-pro-512gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074302/18307dfde0f655618d822607bda8c931_juyktb.jpg', 4, NOW() FROM product WHERE slug = 'macbook-pro-14-m3-pro-512gb' AND status = 'ACTIVE';

-- =============================================================================
-- 12. DELL XPS 15 9530
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074393/94a6a956eb97045f96d7ef7aae280ec6_zsdai8.jpg',
    updated_at = NOW()
WHERE slug = 'dell-xps-15-9530-i7-rtx4060' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074393/94a6a956eb97045f96d7ef7aae280ec6_zsdai8.jpg', 1, NOW() FROM product WHERE slug = 'dell-xps-15-9530-i7-rtx4060' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074393/94a6a956eb97045f96d7ef7aae280ec6_zsdai8.jpg', 2, NOW() FROM product WHERE slug = 'dell-xps-15-9530-i7-rtx4060' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074393/94a6a956eb97045f96d7ef7aae280ec6_zsdai8.jpg', 3, NOW() FROM product WHERE slug = 'dell-xps-15-9530-i7-rtx4060' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074393/94a6a956eb97045f96d7ef7aae280ec6_zsdai8.jpg', 4, NOW() FROM product WHERE slug = 'dell-xps-15-9530-i7-rtx4060' AND status = 'ACTIVE';

-- =============================================================================
-- 13. ASUS ROG STRIX G16
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074441/df0c308856458afe4880edfac72185da_n0nw9h.jpg',
    updated_at = NOW()
WHERE slug = 'asus-rog-strix-g16-i9-rtx4070' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074450/852ddd83f760921f00ec841046b94256_qir4ci.jpg', 1, NOW() FROM product WHERE slug = 'asus-rog-strix-g16-i9-rtx4070' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074441/df0c308856458afe4880edfac72185da_n0nw9h.jpg', 2, NOW() FROM product WHERE slug = 'asus-rog-strix-g16-i9-rtx4070' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074450/852ddd83f760921f00ec841046b94256_qir4ci.jpg', 3, NOW() FROM product WHERE slug = 'asus-rog-strix-g16-i9-rtx4070' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074441/df0c308856458afe4880edfac72185da_n0nw9h.jpg', 4, NOW() FROM product WHERE slug = 'asus-rog-strix-g16-i9-rtx4070' AND status = 'ACTIVE';

-- =============================================================================
-- 14. LENOVO THINKPAD X1 CARBON
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074497/0164fa00a7c4d6404557b5f54ae190d1_ecpq6u.jpg',
    updated_at = NOW()
WHERE slug = 'lenovo-thinkpad-x1-carbon-gen11' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074498/6eefea681b313499dcd6005dc75f1e47_gcs04r.jpg', 1, NOW() FROM product WHERE slug = 'lenovo-thinkpad-x1-carbon-gen11' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074497/0164fa00a7c4d6404557b5f54ae190d1_ecpq6u.jpg', 2, NOW() FROM product WHERE slug = 'lenovo-thinkpad-x1-carbon-gen11' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074498/6eefea681b313499dcd6005dc75f1e47_gcs04r.jpg', 3, NOW() FROM product WHERE slug = 'lenovo-thinkpad-x1-carbon-gen11' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074497/0164fa00a7c4d6404557b5f54ae190d1_ecpq6u.jpg', 4, NOW() FROM product WHERE slug = 'lenovo-thinkpad-x1-carbon-gen11' AND status = 'ACTIVE';

-- =============================================================================
-- 15. MACBOOK AIR 15 M3
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074549/68ca6326e6fbb0fca322cf07305d8cbe_ldbrxd.jpg',
    updated_at = NOW()
WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074551/91e9d7f4d51729dee6f7210c663f5bc4_ot6fiq.jpg', 1, NOW() FROM product WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074549/68ca6326e6fbb0fca322cf07305d8cbe_ldbrxd.jpg', 2, NOW() FROM product WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074551/91e9d7f4d51729dee6f7210c663f5bc4_ot6fiq.jpg', 3, NOW() FROM product WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074549/68ca6326e6fbb0fca322cf07305d8cbe_ldbrxd.jpg', 4, NOW() FROM product WHERE slug = 'macbook-air-15-m3-256gb' AND status = 'ACTIVE';

-- =============================================================================
-- 16. APPLE AIRPODS PRO 2
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074613/f31794f2a6bc4a7305528502bfcb9a9c_wmedml.jpg',
    updated_at = NOW()
WHERE slug = 'apple-airpods-pro-2-usb-c' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074614/e77d51fc35ea2424f1e76ca5ebbdae68_zzpkoh.jpg', 1, NOW() FROM product WHERE slug = 'apple-airpods-pro-2-usb-c' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074613/f31794f2a6bc4a7305528502bfcb9a9c_wmedml.jpg', 2, NOW() FROM product WHERE slug = 'apple-airpods-pro-2-usb-c' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074614/e77d51fc35ea2424f1e76ca5ebbdae68_zzpkoh.jpg', 3, NOW() FROM product WHERE slug = 'apple-airpods-pro-2-usb-c' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074613/f31794f2a6bc4a7305528502bfcb9a9c_wmedml.jpg', 4, NOW() FROM product WHERE slug = 'apple-airpods-pro-2-usb-c' AND status = 'ACTIVE';

-- =============================================================================
-- 17. SAMSUNG GALAXY BUDS3 PRO
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074665/f6834955760f5b90fed357a38d8cdcb4_quskdu.jpg',
    updated_at = NOW()
WHERE slug = 'samsung-galaxy-buds3-pro' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074663/5af47bfddbf1288d74deb0ff54d2c37a_hf2yoh.jpg', 1, NOW() FROM product WHERE slug = 'samsung-galaxy-buds3-pro' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074665/f6834955760f5b90fed357a38d8cdcb4_quskdu.jpg', 2, NOW() FROM product WHERE slug = 'samsung-galaxy-buds3-pro' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074663/5af47bfddbf1288d74deb0ff54d2c37a_hf2yoh.jpg', 3, NOW() FROM product WHERE slug = 'samsung-galaxy-buds3-pro' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074665/f6834955760f5b90fed357a38d8cdcb4_quskdu.jpg', 4, NOW() FROM product WHERE slug = 'samsung-galaxy-buds3-pro' AND status = 'ACTIVE';

-- =============================================================================
-- 18. APPLE WATCH SERIES 9
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074714/c72a287af1752c8073a4d084c9dd31f4_mkktyp.jpg',
    updated_at = NOW()
WHERE slug = 'apple-watch-series-9-gps-45mm' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074717/239b131b38f39116b590a9d96c5b86ba_idjw97.jpg', 1, NOW() FROM product WHERE slug = 'apple-watch-series-9-gps-45mm' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074714/c72a287af1752c8073a4d084c9dd31f4_mkktyp.jpg', 2, NOW() FROM product WHERE slug = 'apple-watch-series-9-gps-45mm' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074717/239b131b38f39116b590a9d96c5b86ba_idjw97.jpg', 3, NOW() FROM product WHERE slug = 'apple-watch-series-9-gps-45mm' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074714/c72a287af1752c8073a4d084c9dd31f4_mkktyp.jpg', 4, NOW() FROM product WHERE slug = 'apple-watch-series-9-gps-45mm' AND status = 'ACTIVE';

-- =============================================================================
-- 19. SAMSUNG GALAXY WATCH 6 CLASSIC
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074761/63b138fd4ffc10435531a84214de2d3a_f6fvgi.jpg',
    updated_at = NOW()
WHERE slug = 'samsung-galaxy-watch-6-classic-47mm' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074763/35bc5f39a68a02d993b9f2b9799dedc9_y4nm1v.jpg', 1, NOW() FROM product WHERE slug = 'samsung-galaxy-watch-6-classic-47mm' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074761/63b138fd4ffc10435531a84214de2d3a_f6fvgi.jpg', 2, NOW() FROM product WHERE slug = 'samsung-galaxy-watch-6-classic-47mm' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074763/35bc5f39a68a02d993b9f2b9799dedc9_y4nm1v.jpg', 3, NOW() FROM product WHERE slug = 'samsung-galaxy-watch-6-classic-47mm' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074761/63b138fd4ffc10435531a84214de2d3a_f6fvgi.jpg', 4, NOW() FROM product WHERE slug = 'samsung-galaxy-watch-6-classic-47mm' AND status = 'ACTIVE';

-- =============================================================================
-- 20. NIKE AIR FORCE 1 07
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074811/e1b6f4d3c856dd7c5f5bdc9c1b2e51ad_zut0uw.jpg',
    updated_at = NOW()
WHERE slug = 'nike-air-force-1-07-triple-white' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074813/a0bbfcf435aef827455be127f948a758_jhue4y.jpg', 1, NOW() FROM product WHERE slug = 'nike-air-force-1-07-triple-white' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074811/e1b6f4d3c856dd7c5f5bdc9c1b2e51ad_zut0uw.jpg', 2, NOW() FROM product WHERE slug = 'nike-air-force-1-07-triple-white' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074813/a0bbfcf435aef827455be127f948a758_jhue4y.jpg', 3, NOW() FROM product WHERE slug = 'nike-air-force-1-07-triple-white' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074811/e1b6f4d3c856dd7c5f5bdc9c1b2e51ad_zut0uw.jpg', 4, NOW() FROM product WHERE slug = 'nike-air-force-1-07-triple-white' AND status = 'ACTIVE';

-- =============================================================================
-- 21. ADIDAS ULTRABOOST 23
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074872/5fd58e4b79428d6cacb059e20cd3adaa_b6tjmb.jpg',
    updated_at = NOW()
WHERE slug = 'adidas-ultraboost-23-core-black' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074874/2350a6d4f98549305189a44a24d5fc0a_x2lxut.jpg', 1, NOW() FROM product WHERE slug = 'adidas-ultraboost-23-core-black' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074872/5fd58e4b79428d6cacb059e20cd3adaa_b6tjmb.jpg', 2, NOW() FROM product WHERE slug = 'adidas-ultraboost-23-core-black' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074874/2350a6d4f98549305189a44a24d5fc0a_x2lxut.jpg', 3, NOW() FROM product WHERE slug = 'adidas-ultraboost-23-core-black' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074872/5fd58e4b79428d6cacb059e20cd3adaa_b6tjmb.jpg', 4, NOW() FROM product WHERE slug = 'adidas-ultraboost-23-core-black' AND status = 'ACTIVE';

-- =============================================================================
-- 22. ÁO POLO UNIQLO DRY-EX
-- =============================================================================
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074919/33f3ecdd0870d6c1298f70e1a5987aea_cwjspu.jpg',
    updated_at = NOW()
WHERE slug = 'ao-polo-uniqlo-dry-ex-navy' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074921/54d3893733a1ae78c65be187b0b67379_fpswhu.jpg', 1, NOW() FROM product WHERE slug = 'ao-polo-uniqlo-dry-ex-navy' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074919/33f3ecdd0870d6c1298f70e1a5987aea_cwjspu.jpg', 2, NOW() FROM product WHERE slug = 'ao-polo-uniqlo-dry-ex-navy' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074921/54d3893733a1ae78c65be187b0b67379_fpswhu.jpg', 3, NOW() FROM product WHERE slug = 'ao-polo-uniqlo-dry-ex-navy' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769074919/33f3ecdd0870d6c1298f70e1a5987aea_cwjspu.jpg', 4, NOW() FROM product WHERE slug = 'ao-polo-uniqlo-dry-ex-navy' AND status = 'ACTIVE';

-- =============================================================================
-- HƯỚNG DẪN SỬ DỤNG:
-- =============================================================================
-- 1. Để thêm sản phẩm mới, copy một section (từ "-- =====..." đến hết INSERT)
-- 2. Thay đổi số thứ tự và tên sản phẩm
-- 3. Thay đổi slug trong WHERE clause
-- 4. Thay đổi các URL hình ảnh (4 ảnh khác nhau)
-- 5. Thêm slug vào danh sách DELETE ở đầu file
-- =============================================================================
