-- =============================================================================
-- Migration: Update Remaining Product Images
-- Description: Cập nhật hình ảnh cho các sản phẩm còn lại chưa được cập nhật
--              Mỗi sản phẩm có 4 ảnh khác nhau
-- =============================================================================

-- Xóa product_image cũ của các sản phẩm được cập nhật
DELETE pi FROM product_image pi
INNER JOIN product p ON p.id = pi.product_id
WHERE p.status = 'ACTIVE'
  AND p.slug IN (
    -- Điện thoại
    'vivo-x100-pro-512gb-zeiss',
    'realme-gt-5-pro-256gb',
    'oneplus-12-256gb',
    -- Laptop
    'hp-pavilion-plus-14-i5',
    'acer-predator-helios-16-rtx4060',
    'msi-katana-15-rtx4050',
    'asus-vivobook-15-ryzen5',
    'lenovo-ideapad-3-i3',
    -- Phụ kiện
    'sony-wh-1000xm5',
    'jbl-flip-6-portable',
    'logitech-mx-master-3s',
    'razer-deathadder-v3-pro',
    -- Thời trang
    'adidas-superstar-classic-white',
    'converse-chuck-taylor-all-star',
    'nike-dunk-low-retro',
    'puma-suede-classic',
    -- Quần áo
    'ao-thun-nike-dri-fit',
    'quan-jean-levis-501',
    'ao-khoac-north-face',
    'ao-so-mi-unisex'
  );

-- =============================================================================
-- ĐIỆN THOẠI
-- =============================================================================

-- Vivo X100 Pro
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070379/e2c901fe7a59bce2a479706c3cc553d6_uqkttd.jpg',
    updated_at = NOW()
WHERE slug = 'vivo-x100-pro-512gb-zeiss' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070379/e2c901fe7a59bce2a479706c3cc553d6_uqkttd.jpg', 1, NOW() FROM product WHERE slug = 'vivo-x100-pro-512gb-zeiss' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076076/1f69d1b2717c5be010d9787e0b30b3c1_u99drl.jpg', 2, NOW() FROM product WHERE slug = 'vivo-x100-pro-512gb-zeiss' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070379/e2c901fe7a59bce2a479706c3cc553d6_uqkttd.jpg', 3, NOW() FROM product WHERE slug = 'vivo-x100-pro-512gb-zeiss' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076076/1f69d1b2717c5be010d9787e0b30b3c1_u99drl.jpg', 4, NOW() FROM product WHERE slug = 'vivo-x100-pro-512gb-zeiss' AND status = 'ACTIVE';

-- Realme GT 5 Pro
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070440/d6b24d652da0179d9116b0bb7b8c8cad_ws8xgn.jpg',
    updated_at = NOW()
WHERE slug = 'realme-gt-5-pro-256gb' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070440/d6b24d652da0179d9116b0bb7b8c8cad_ws8xgn.jpg', 1, NOW() FROM product WHERE slug = 'realme-gt-5-pro-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076164/d6b24d652da0179d9116b0bb7b8c8cad_bo10ue.jpg', 2, NOW() FROM product WHERE slug = 'realme-gt-5-pro-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076164/d6b24d652da0179d9116b0bb7b8c8cad_bo10ue.jpg', 3, NOW() FROM product WHERE slug = 'realme-gt-5-pro-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076164/d6b24d652da0179d9116b0bb7b8c8cad_bo10ue.jpg', 4, NOW() FROM product WHERE slug = 'realme-gt-5-pro-256gb' AND status = 'ACTIVE';

-- OnePlus 12
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070484/555fd1b76d39017fbb3f667490a88818_wpl5sb.jpg',
    updated_at = NOW()
WHERE slug = 'oneplus-12-256gb' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070484/555fd1b76d39017fbb3f667490a88818_wpl5sb.jpg', 1, NOW() FROM product WHERE slug = 'oneplus-12-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076205/f4bbc937d02a9e17910d956c5c8d7fc4_p177vv.jpg', 2, NOW() FROM product WHERE slug = 'oneplus-12-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070484/555fd1b76d39017fbb3f667490a88818_wpl5sb.jpg', 3, NOW() FROM product WHERE slug = 'oneplus-12-256gb' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076205/f4bbc937d02a9e17910d956c5c8d7fc4_p177vv.jpg', 4, NOW() FROM product WHERE slug = 'oneplus-12-256gb' AND status = 'ACTIVE';

-- =============================================================================
-- LAPTOP
-- =============================================================================

-- HP Pavilion Plus 14
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070526/02b630210da124defa695e80660f4a66_ilvp2x.jpg',
    updated_at = NOW()
WHERE slug = 'hp-pavilion-plus-14-i5' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070526/02b630210da124defa695e80660f4a66_ilvp2x.jpg', 1, NOW() FROM product WHERE slug = 'hp-pavilion-plus-14-i5' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070526/02b630210da124defa695e80660f4a66_ilvp2x.jpg', 2, NOW() FROM product WHERE slug = 'hp-pavilion-plus-14-i5' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076249/7c5a53d982fe2441233e5757e2f93ce3_kvmmki.jpg', 3, NOW() FROM product WHERE slug = 'hp-pavilion-plus-14-i5' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076249/7c5a53d982fe2441233e5757e2f93ce3_kvmmki.jpg', 4, NOW() FROM product WHERE slug = 'hp-pavilion-plus-14-i5' AND status = 'ACTIVE';

-- Acer Predator Helios 16
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070575/3eb7f47ad57f7a2b13c76db8f135ee5d_e1oovx.jpg',
    updated_at = NOW()
WHERE slug = 'acer-predator-helios-16-rtx4060' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070575/3eb7f47ad57f7a2b13c76db8f135ee5d_e1oovx.jpg', 1, NOW() FROM product WHERE slug = 'acer-predator-helios-16-rtx4060' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070575/3eb7f47ad57f7a2b13c76db8f135ee5d_e1oovx.jpg', 2, NOW() FROM product WHERE slug = 'acer-predator-helios-16-rtx4060' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076299/2b8c80f99316732cf9c4ce57c6b0e0e9_yunwm7.jpg', 3, NOW() FROM product WHERE slug = 'acer-predator-helios-16-rtx4060' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076299/2b8c80f99316732cf9c4ce57c6b0e0e9_yunwm7.jpg', 4, NOW() FROM product WHERE slug = 'acer-predator-helios-16-rtx4060' AND status = 'ACTIVE';

-- MSI Katana 15
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070633/9d895d1d087c4facbe743502e1215481_sfqloy.jpg',
    updated_at = NOW()
WHERE slug = 'msi-katana-15-rtx4050' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070633/9d895d1d087c4facbe743502e1215481_sfqloy.jpg', 1, NOW() FROM product WHERE slug = 'msi-katana-15-rtx4050' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076340/1d82bf6a34cbbc8c9f7eedd73771245c_tddcbv.jpg', 2, NOW() FROM product WHERE slug = 'msi-katana-15-rtx4050' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076340/1d82bf6a34cbbc8c9f7eedd73771245c_tddcbv.jpg', 3, NOW() FROM product WHERE slug = 'msi-katana-15-rtx4050' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076340/1d82bf6a34cbbc8c9f7eedd73771245c_tddcbv.jpg', 4, NOW() FROM product WHERE slug = 'msi-katana-15-rtx4050' AND status = 'ACTIVE';

-- ASUS Vivobook 15
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070671/94de4818decdaa616de77c9a7462f0ee_ifhucq.jpg',
    updated_at = NOW()
WHERE slug = 'asus-vivobook-15-ryzen5' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070671/94de4818decdaa616de77c9a7462f0ee_ifhucq.jpg', 1, NOW() FROM product WHERE slug = 'asus-vivobook-15-ryzen5' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076392/e6e9bbab523c88d500e48a88b5484c4e_jlu94b.jpg', 2, NOW() FROM product WHERE slug = 'asus-vivobook-15-ryzen5' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076392/e6e9bbab523c88d500e48a88b5484c4e_jlu94b.jpg', 3, NOW() FROM product WHERE slug = 'asus-vivobook-15-ryzen5' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076392/e6e9bbab523c88d500e48a88b5484c4e_jlu94b.jpg', 4, NOW() FROM product WHERE slug = 'asus-vivobook-15-ryzen5' AND status = 'ACTIVE';

-- Lenovo IdeaPad 3
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070714/06f13f3d14f00ec4c6cb4d50e5a8e072_dkd6b6.jpg',
    updated_at = NOW()
WHERE slug = 'lenovo-ideapad-3-i3' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070714/06f13f3d14f00ec4c6cb4d50e5a8e072_dkd6b6.jpg', 1, NOW() FROM product WHERE slug = 'lenovo-ideapad-3-i3' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076444/0d7568130f4f0c82f5d1551ef8241876_m9thl2.jpg', 2, NOW() FROM product WHERE slug = 'lenovo-ideapad-3-i3' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769070714/06f13f3d14f00ec4c6cb4d50e5a8e072_dkd6b6.jpg', 3, NOW() FROM product WHERE slug = 'lenovo-ideapad-3-i3' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076444/0d7568130f4f0c82f5d1551ef8241876_m9thl2.jpg', 4, NOW() FROM product WHERE slug = 'lenovo-ideapad-3-i3' AND status = 'ACTIVE';

-- =============================================================================
-- PHỤ KIỆN
-- =============================================================================

-- Sony WH-1000XM5
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076493/3f844c106259777957ca8988fefac46f_jvoars.jpg',
    updated_at = NOW()
WHERE slug = 'sony-wh-1000xm5' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076498/5bfc2af3cc198245a113ab55600bc0f1_bsr3sr.jpg', 1, NOW() FROM product WHERE slug = 'sony-wh-1000xm5' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076493/3f844c106259777957ca8988fefac46f_jvoars.jpg', 2, NOW() FROM product WHERE slug = 'sony-wh-1000xm5' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076498/5bfc2af3cc198245a113ab55600bc0f1_bsr3sr.jpg', 3, NOW() FROM product WHERE slug = 'sony-wh-1000xm5' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076493/3f844c106259777957ca8988fefac46f_jvoars.jpg', 4, NOW() FROM product WHERE slug = 'sony-wh-1000xm5' AND status = 'ACTIVE';

-- JBL Flip 6
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076541/8484ba19f8baa43ec8118714ed644800_ilp9tt.jpg',
    updated_at = NOW()
WHERE slug = 'jbl-flip-6-portable' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076539/93b323740d2d2a645a9c47f716e29578_wtppp4.jpg', 1, NOW() FROM product WHERE slug = 'jbl-flip-6-portable' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076541/8484ba19f8baa43ec8118714ed644800_ilp9tt.jpg', 2, NOW() FROM product WHERE slug = 'jbl-flip-6-portable' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076541/8484ba19f8baa43ec8118714ed644800_ilp9tt.jpg', 3, NOW() FROM product WHERE slug = 'jbl-flip-6-portable' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076539/93b323740d2d2a645a9c47f716e29578_wtppp4.jpg', 4, NOW() FROM product WHERE slug = 'jbl-flip-6-portable' AND status = 'ACTIVE';

-- Logitech MX Master 3S
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076604/f9c12499e73c9ff95f64607083f2f6dc_ffil3v.jpg',
    updated_at = NOW()
WHERE slug = 'logitech-mx-master-3s' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076609/55de01ec392001518475ecac0ea5b8db_a0cuym.jpg', 1, NOW() FROM product WHERE slug = 'logitech-mx-master-3s' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076604/f9c12499e73c9ff95f64607083f2f6dc_ffil3v.jpg', 2, NOW() FROM product WHERE slug = 'logitech-mx-master-3s' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076609/55de01ec392001518475ecac0ea5b8db_a0cuym.jpg', 3, NOW() FROM product WHERE slug = 'logitech-mx-master-3s' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076604/f9c12499e73c9ff95f64607083f2f6dc_ffil3v.jpg', 4, NOW() FROM product WHERE slug = 'logitech-mx-master-3s' AND status = 'ACTIVE';

-- Razer DeathAdder V3 Pro
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076654/2de5cedbb7d9ccbea1323d7dc2c64784_kuv97d.jpg',
    updated_at = NOW()
WHERE slug = 'razer-deathadder-v3-pro' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076658/c3e9f555d377d3c8be2a166473de3106_ucjoca.jpg', 1, NOW() FROM product WHERE slug = 'razer-deathadder-v3-pro' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076654/2de5cedbb7d9ccbea1323d7dc2c64784_kuv97d.jpg', 2, NOW() FROM product WHERE slug = 'razer-deathadder-v3-pro' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076658/c3e9f555d377d3c8be2a166473de3106_ucjoca.jpg', 3, NOW() FROM product WHERE slug = 'razer-deathadder-v3-pro' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076654/2de5cedbb7d9ccbea1323d7dc2c64784_kuv97d.jpg', 4, NOW() FROM product WHERE slug = 'razer-deathadder-v3-pro' AND status = 'ACTIVE';

-- =============================================================================
-- GIÀY DÉP
-- =============================================================================

-- Adidas Superstar
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076718/38f3021ed174f8d75cfa460bb840d14f_odxtrd.jpg',
    updated_at = NOW()
WHERE slug = 'adidas-superstar-classic-white' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076739/dd610bafcc55fe883290b51596c84edf_kudaxh.jpg', 1, NOW() FROM product WHERE slug = 'adidas-superstar-classic-white' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076718/38f3021ed174f8d75cfa460bb840d14f_odxtrd.jpg', 2, NOW() FROM product WHERE slug = 'adidas-superstar-classic-white' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076739/dd610bafcc55fe883290b51596c84edf_kudaxh.jpg', 3, NOW() FROM product WHERE slug = 'adidas-superstar-classic-white' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076718/38f3021ed174f8d75cfa460bb840d14f_odxtrd.jpg', 4, NOW() FROM product WHERE slug = 'adidas-superstar-classic-white' AND status = 'ACTIVE';

-- Converse Chuck Taylor
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076776/1c2e4f51a3d864bade99038e3e478a98_fewvfu.jpg',
    updated_at = NOW()
WHERE slug = 'converse-chuck-taylor-all-star' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076781/a3917d22bfa9f4973ab57b9a2ecf7300_qnzynh.jpg', 1, NOW() FROM product WHERE slug = 'converse-chuck-taylor-all-star' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076776/1c2e4f51a3d864bade99038e3e478a98_fewvfu.jpg', 2, NOW() FROM product WHERE slug = 'converse-chuck-taylor-all-star' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076781/a3917d22bfa9f4973ab57b9a2ecf7300_qnzynh.jpg', 3, NOW() FROM product WHERE slug = 'converse-chuck-taylor-all-star' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076776/1c2e4f51a3d864bade99038e3e478a98_fewvfu.jpg', 4, NOW() FROM product WHERE slug = 'converse-chuck-taylor-all-star' AND status = 'ACTIVE';

-- Nike Dunk Low
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076828/a137676adce11fd44f881c9f8893bd7f_ltner8.jpg',
    updated_at = NOW()
WHERE slug = 'nike-dunk-low-retro' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076832/121c5a52249cf5e6ee098baa09001028_vra2lw.jpg', 1, NOW() FROM product WHERE slug = 'nike-dunk-low-retro' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076828/a137676adce11fd44f881c9f8893bd7f_ltner8.jpg', 2, NOW() FROM product WHERE slug = 'nike-dunk-low-retro' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076832/121c5a52249cf5e6ee098baa09001028_vra2lw.jpg', 3, NOW() FROM product WHERE slug = 'nike-dunk-low-retro' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076828/a137676adce11fd44f881c9f8893bd7f_ltner8.jpg', 4, NOW() FROM product WHERE slug = 'nike-dunk-low-retro' AND status = 'ACTIVE';

-- Puma Suede Classic
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076886/6ff2642f6f9f2a96725e289098d65f93_xamcwx.jpg',
    updated_at = NOW()
WHERE slug = 'puma-suede-classic' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076886/6ff2642f6f9f2a96725e289098d65f93_xamcwx.jpg', 1, NOW() FROM product WHERE slug = 'puma-suede-classic' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076718/38f3021ed174f8d75cfa460bb840d14f_odxtrd.jpg', 2, NOW() FROM product WHERE slug = 'puma-suede-classic' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076739/dd610bafcc55fe883290b51596c84edf_kudaxh.jpg', 3, NOW() FROM product WHERE slug = 'puma-suede-classic' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076718/38f3021ed174f8d75cfa460bb840d14f_odxtrd.jpg', 4, NOW() FROM product WHERE slug = 'puma-suede-classic' AND status = 'ACTIVE';

-- =============================================================================
-- QUẦN ÁO
-- =============================================================================

-- Áo thun Nike
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076932/c9b82e47df8b2ce1d086aa0fb328adec_rhuxmb.jpg',
    updated_at = NOW()
WHERE slug = 'ao-thun-nike-dri-fit' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076932/c9b82e47df8b2ce1d086aa0fb328adec_rhuxmb.jpg', 1, NOW() FROM product WHERE slug = 'ao-thun-nike-dri-fit' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076932/c9b82e47df8b2ce1d086aa0fb328adec_rhuxmb.jpg', 2, NOW() FROM product WHERE slug = 'ao-thun-nike-dri-fit' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076932/c9b82e47df8b2ce1d086aa0fb328adec_rhuxmb.jpg', 3, NOW() FROM product WHERE slug = 'ao-thun-nike-dri-fit' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076932/c9b82e47df8b2ce1d086aa0fb328adec_rhuxmb.jpg', 4, NOW() FROM product WHERE slug = 'ao-thun-nike-dri-fit' AND status = 'ACTIVE';

-- Quần jean Levis
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076969/3acb4b17f56e135b8f72152f3de66d30_njmuap.jpg',
    updated_at = NOW()
WHERE slug = 'quan-jean-levis-501' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076969/3acb4b17f56e135b8f72152f3de66d30_njmuap.jpg', 1, NOW() FROM product WHERE slug = 'quan-jean-levis-501' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076969/3acb4b17f56e135b8f72152f3de66d30_njmuap.jpg', 2, NOW() FROM product WHERE slug = 'quan-jean-levis-501' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076969/3acb4b17f56e135b8f72152f3de66d30_njmuap.jpg', 3, NOW() FROM product WHERE slug = 'quan-jean-levis-501' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769076969/3acb4b17f56e135b8f72152f3de66d30_njmuap.jpg', 4, NOW() FROM product WHERE slug = 'quan-jean-levis-501' AND status = 'ACTIVE';

-- Áo khoác North Face
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077013/19e41a0c621ae6d1de599dcbb73f71d3_maq76o.jpg',
    updated_at = NOW()
WHERE slug = 'ao-khoac-north-face' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077013/19e41a0c621ae6d1de599dcbb73f71d3_maq76o.jpg', 1, NOW() FROM product WHERE slug = 'ao-khoac-north-face' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077013/19e41a0c621ae6d1de599dcbb73f71d3_maq76o.jpg', 2, NOW() FROM product WHERE slug = 'ao-khoac-north-face' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077013/19e41a0c621ae6d1de599dcbb73f71d3_maq76o.jpg', 3, NOW() FROM product WHERE slug = 'ao-khoac-north-face' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077013/19e41a0c621ae6d1de599dcbb73f71d3_maq76o.jpg', 4, NOW() FROM product WHERE slug = 'ao-khoac-north-face' AND status = 'ACTIVE';

-- Áo sơ mi unisex
UPDATE product 
SET main_image_url = 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077059/f5f50ac5b55e93f6b7946d75ba8850ee_agrurn.jpg',
    updated_at = NOW()
WHERE slug = 'ao-so-mi-unisex' AND status = 'ACTIVE';

INSERT INTO product_image (product_id, image_url, sort_order, created_at)
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077059/f5f50ac5b55e93f6b7946d75ba8850ee_agrurn.jpg', 1, NOW() FROM product WHERE slug = 'ao-so-mi-unisex' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077059/f5f50ac5b55e93f6b7946d75ba8850ee_agrurn.jpg', 2, NOW() FROM product WHERE slug = 'ao-so-mi-unisex' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077059/f5f50ac5b55e93f6b7946d75ba8850ee_agrurn.jpg', 3, NOW() FROM product WHERE slug = 'ao-so-mi-unisex' AND status = 'ACTIVE'
UNION ALL
SELECT id, 'https://res.cloudinary.com/dajzp6qro/image/upload/v1769077059/f5f50ac5b55e93f6b7946d75ba8850ee_agrurn.jpg', 4, NOW() FROM product WHERE slug = 'ao-so-mi-unisex' AND status = 'ACTIVE';
