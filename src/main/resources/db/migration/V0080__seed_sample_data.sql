SET FOREIGN_KEY_CHECKS=0;

INSERT INTO role(code, name)
SELECT * FROM (SELECT 'ADMIN' as c,'Administrator' as n) AS x
WHERE NOT EXISTS (SELECT 1 FROM role WHERE code='ADMIN');
INSERT INTO role(code, name)
SELECT * FROM (SELECT 'SELLER' as c,'Seller' as n) AS x
WHERE NOT EXISTS (SELECT 1 FROM role WHERE code='SELLER');
INSERT INTO role(code, name)
SELECT * FROM (SELECT 'CLIENT' as c,'Client' as n) AS x
WHERE NOT EXISTS (SELECT 1 FROM role WHERE code='CLIENT');

INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'admin@gmail.com', 'Password123!', 'System Admin', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='admin@gmail.com');
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'seller1@gmail.com', 'Password123!', 'Seller One', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='seller1@gmail.com');
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'seller2@gmail.com', 'Password123!', 'Seller Two', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='seller2@gmail.com');
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'client1@gmail.com', 'Password123!', 'Client One', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='client1@gmail.com');
INSERT INTO app_user (email, password_hash, full_name, status, created_at, updated_at)
SELECT 'client2@gmail.com', 'Password123!', 'Client Two', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE email='client2@gmail.com');

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.code = 'ADMIN'
WHERE u.email = 'admin@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.code = 'SELLER'
WHERE u.email IN ('seller1@gmail.com', 'seller2@gmail.com')
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.code = 'CLIENT'
WHERE u.email IN ('client1@gmail.com', 'client2@gmail.com')
  AND NOT EXISTS (SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO user_profile (user_id, phone, avatar_url, updated_at)
SELECT u.id, '0900000004', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=128&h=128&q=80', NOW()
FROM app_user u
WHERE u.email = 'client1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM user_profile p WHERE p.user_id = u.id);

INSERT INTO user_profile (user_id, phone, avatar_url, updated_at)
SELECT u.id, '0900000005', 'https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?auto=format&fit=crop&w=128&h=128&q=80', NOW()
FROM app_user u
WHERE u.email = 'client2@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM user_profile p WHERE p.user_id = u.id);

INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Client One', '0900000004', '123 ABC Street', 'Ward 1', 'District 1', 'HCM', TRUE
FROM app_user u
WHERE u.email = 'client1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM user_address a WHERE a.user_id = u.id AND a.is_default = TRUE);
INSERT INTO user_address (user_id, receiver_name, receiver_phone, line1, ward, district, province, is_default)
SELECT u.id, 'Client Two', '0900000005', '456 XYZ Street', 'Ward 2', 'District 2', 'HCM', TRUE
FROM app_user u
WHERE u.email = 'client2@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM user_address a WHERE a.user_id = u.id AND a.is_default = TRUE);

INSERT INTO seller_shop (seller_user_id, shop_name, shop_slug, status, created_at)
SELECT u.id, 'Apple Store Official', 'apple-store-official', 'ACTIVE', NOW()
FROM app_user u
WHERE u.email = 'seller1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM seller_shop s WHERE s.seller_user_id = u.id);

INSERT INTO seller_shop (seller_user_id, shop_name, shop_slug, status, created_at)
SELECT u.id, 'Samsung Hub', 'samsung-hub', 'ACTIVE', NOW()
FROM app_user u
WHERE u.email = 'seller2@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM seller_shop s WHERE s.seller_user_id = u.id);

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Electronics', 'electronics', '/electronics', NULL, TRUE, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Electronics');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Phones', 'phones', '/electronics/phones', (SELECT id FROM category WHERE name = 'Electronics'), TRUE, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Phones');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Laptops', 'laptops', '/electronics/laptops', (SELECT id FROM category WHERE name = 'Electronics'), TRUE, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Laptops');

INSERT INTO category (name, slug, path, parent_id, is_active, sort_order, created_at, updated_at)
SELECT 'Fashion', 'fashion', '/fashion', NULL, TRUE, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Fashion');

INSERT INTO brand (name, slug, is_active, created_at, updated_at)
SELECT 'Apple', 'apple', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = 'Apple');

INSERT INTO brand (name, slug, is_active, created_at, updated_at)
SELECT 'Samsung', 'samsung', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = 'Samsung');

INSERT INTO brand (name, slug, is_active, created_at, updated_at)
SELECT 'Nike', 'nike', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM brand WHERE name = 'Nike');

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id, 'iPhone 15 Pro', 'iphone-15-pro', 'Flagship smartphone', 'https://via.placeholder.com/300', 29990000, 50, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official' AND c.name = 'Phones' AND b.name = 'Apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'iPhone 15 Pro' AND p.shop_id = s.id);

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id, 'MacBook Air M3', 'macbook-air-m3', 'Lightweight laptop', 'https://via.placeholder.com/300', 25990000, 30, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Apple Store Official' AND c.name = 'Laptops' AND b.name = 'Apple'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'MacBook Air M3' AND p.shop_id = s.id);

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id, 'Galaxy S24', 'galaxy-s24', 'Premium Android phone', 'https://via.placeholder.com/300', 20000000, 50, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Samsung Hub' AND c.name = 'Phones' AND b.name = 'Samsung'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Galaxy S24' AND p.shop_id = s.id);

INSERT INTO product (shop_id, category_id, brand_id, seller_user_id, name, slug, description, main_image_url, price, stock_quantity, currency, status, created_at, updated_at)
SELECT s.id, c.id, b.id, s.seller_user_id, 'Running Shoes Pro', 'running-shoes-pro', 'Comfortable running shoes', 'https://via.placeholder.com/300', 2000000, 100, 'VND', 'ACTIVE', NOW(), NOW()
FROM seller_shop s, category c, brand b
WHERE s.shop_name = 'Samsung Hub' AND c.name = 'Fashion' AND b.name = 'Nike'
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'Running Shoes Pro' AND p.shop_id = s.id);

INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'IP15P-128', 29990000, 50, 0, 'default', MD5('IP15P-128'), NOW(), NOW()
FROM product p
WHERE p.name = 'iPhone 15 Pro'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'IP15P-128');
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'IP15P-256', 32990000, 30, 0, 'default', MD5('IP15P-256'), NOW(), NOW()
FROM product p
WHERE p.name = 'iPhone 15 Pro'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'IP15P-256');
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'MBA-M3-256', 25990000, 20, 0, 'default', MD5('MBA-M3-256'), NOW(), NOW()
FROM product p
WHERE p.name = 'MacBook Air M3'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'MBA-M3-256');
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'S24-128', 22990000, 40, 0, 'default', MD5('S24-128'), NOW(), NOW()
FROM product p
WHERE p.name = 'Galaxy S24'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'S24-128');
INSERT INTO product_sku (product_id, sku_code, price, stock_on_hand, reserved_stock, option_signature, option_signature_hash, created_at, updated_at)
SELECT p.id, 'RUNPRO-42', 1999000, 100, 0, 'default', MD5('RUNPRO-42'), NOW(), NOW()
FROM product p
WHERE p.name = 'Running Shoes Pro'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'RUNPRO-42');

INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=800&q=80', 1
FROM product p
WHERE p.name = 'iPhone 15 Pro'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 1);
INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1510552776732-03e61cf4b144?auto=format&fit=crop&w=800&q=80', 2
FROM product p
WHERE p.name = 'iPhone 15 Pro'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 2);
INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80', 1
FROM product p
WHERE p.name = 'MacBook Air M3'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 1);
INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1512499617640-c2f999098c01?auto=format&fit=crop&w=800&q=80', 1
FROM product p
WHERE p.name = 'Galaxy S24'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 1);
INSERT INTO product_image (product_id, image_url, sort_order)
SELECT p.id, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 1
FROM product p
WHERE p.name = 'Running Shoes Pro'
  AND NOT EXISTS (SELECT 1 FROM product_image i WHERE i.product_id = p.id AND i.sort_order = 1);

INSERT INTO stock_movement (sku_id, delta, reason, actor_id, idem_scope, idem_key, created_at)
SELECT s.id, 50, 'INITIAL_STOCK', u.id, 'seed', 'stock-ip15p-128', NOW()
FROM product_sku s, app_user u
WHERE s.sku_code = 'IP15P-128'
  AND u.email = 'seller1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM stock_movement m WHERE m.idem_scope = 'seed' AND m.idem_key = 'stock-ip15p-128');
INSERT INTO stock_movement (sku_id, delta, reason, actor_id, idem_scope, idem_key, created_at)
SELECT s.id, 40, 'INITIAL_STOCK', u.id, 'seed', 'stock-s24-128', NOW()
FROM product_sku s, app_user u
WHERE s.sku_code = 'S24-128'
  AND u.email = 'seller2@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM stock_movement m WHERE m.idem_scope = 'seed' AND m.idem_key = 'stock-s24-128');

INSERT INTO stock_reservation (order_token, sku_id, qty, status, created_at, updated_at)
SELECT 'TOKEN-ORD-2026-0001', s.id, 1, 'RESERVED', NOW(), NOW()
FROM product_sku s
WHERE s.sku_code = 'IP15P-128'
  AND NOT EXISTS (SELECT 1 FROM stock_reservation r WHERE r.order_token = 'TOKEN-ORD-2026-0001' AND r.sku_id = s.id);

INSERT INTO orders (order_code, user_id, shop_id, status, total_amount, currency, created_at, updated_at)
SELECT 'ORD-2026-0001', u.id, s.id, 'PAID', 29990000, 'VND', NOW(), NOW()
FROM app_user u, seller_shop s
WHERE u.email = 'client1@gmail.com'
  AND s.shop_name = 'Apple Store Official'
  AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_code = 'ORD-2026-0001');
INSERT INTO orders (order_code, user_id, shop_id, status, total_amount, currency, created_at, updated_at)
SELECT 'ORD-2026-0002', u.id, s.id, 'SHIPPED', 22990000, 'VND', NOW(), NOW()
FROM app_user u, seller_shop s
WHERE u.email = 'client2@gmail.com'
  AND s.shop_name = 'Samsung Hub'
  AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_code = 'ORD-2026-0002');

INSERT INTO order_item (order_id, product_id, sku_id, quantity, unit_price, total_price)
SELECT o.id, p.id, s.id, 1, 29990000, 29990000
FROM orders o
JOIN product p ON p.name = 'iPhone 15 Pro'
JOIN product_sku s ON s.sku_code = 'IP15P-128'
WHERE o.order_code = 'ORD-2026-0001'
  AND NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.id AND i.sku_id = s.id);
INSERT INTO order_item (order_id, product_id, sku_id, quantity, unit_price, total_price)
SELECT o.id, p.id, s.id, 1, 22990000, 22990000
FROM orders o
JOIN product p ON p.name = 'Galaxy S24'
JOIN product_sku s ON s.sku_code = 'S24-128'
WHERE o.order_code = 'ORD-2026-0002'
  AND NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.id AND i.sku_id = s.id);

INSERT INTO cart_item (user_id, shop_id, product_id, sku_id, quantity, created_at, updated_at)
SELECT u.id, sh.id, p.id, s.id, 1, NOW(), NOW()
FROM app_user u
JOIN seller_shop sh ON sh.shop_name = 'Apple Store Official'
JOIN product p ON p.name = 'MacBook Air M3'
JOIN product_sku s ON s.sku_code = 'MBA-M3-256'
WHERE u.email = 'client2@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM cart_item c WHERE c.user_id = u.id AND c.sku_id = s.id);

INSERT INTO payments (order_id, amount, currency, method, status, transaction_id, gateway, gateway_response, created_at, updated_at)
SELECT o.id, 29990000, 'VND', 'VNPAY', 'COMPLETED', 'VNPAY-0001', 'VNPAY', '{"mock":true}', NOW(), NOW()
FROM orders o
WHERE o.order_code = 'ORD-2026-0001'
  AND NOT EXISTS (SELECT 1 FROM payments p WHERE p.order_id = o.id);

INSERT INTO review (product_id, user_id, order_id, rating, comment, images, status, created_at)
SELECT p.id, u.id, o.id, 5, 'Great product and fast delivery', '["https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=800&q=80"]', 'APPROVED', NOW()
FROM product p, app_user u, orders o
WHERE p.name = 'iPhone 15 Pro'
  AND u.email = 'client1@gmail.com'
  AND o.order_code = 'ORD-2026-0001'
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.product_id = p.id AND r.user_id = u.id);

INSERT INTO wishlist_item (user_id, product_id, added_at, note)
SELECT u.id, p.id, NOW(), 'Wishlist item'
FROM app_user u, product p
WHERE u.email = 'client2@gmail.com'
  AND p.name = 'MacBook Air M3'
  AND NOT EXISTS (SELECT 1 FROM wishlist_item w WHERE w.user_id = u.id AND w.product_id = p.id);

INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount, min_order_amount,
  start_date, end_date, usage_limit, usage_count, usage_limit_per_user, auto_apply,
  applicable_product_ids, applicable_category_ids, applicable_user_ids, created_at, updated_at)
SELECT 'WELCOME10', 'Welcome 10%', '10 percent off for new users', 'PERCENTAGE', 'ACTIVE', 10, 100000, 500000,
  DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 100, 0, 1, FALSE,
  NULL, NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'WELCOME10');
INSERT INTO coupon (code, name, description, type, status, discount_value, max_discount_amount, min_order_amount,
  start_date, end_date, usage_limit, usage_count, usage_limit_per_user, auto_apply,
  applicable_product_ids, applicable_category_ids, applicable_user_ids, created_at, updated_at)
SELECT 'SAVE500', 'Save 500K', 'Fixed discount for large orders', 'FIXED_AMOUNT', 'ACTIVE', 500000, NULL, 2000000,
  DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 50, 0, 1, FALSE,
  NULL, NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon WHERE code = 'SAVE500');

INSERT INTO coupon_usage (coupon_id, user_id, order_id, discount_amount, used_at)
SELECT c.id, u.id, o.id, 100000, NOW()
FROM coupon c, app_user u, orders o
WHERE c.code = 'WELCOME10'
  AND u.email = 'client1@gmail.com'
  AND o.order_code = 'ORD-2026-0001'
  AND NOT EXISTS (SELECT 1 FROM coupon_usage cu WHERE cu.coupon_id = c.id AND cu.order_id = o.id);

INSERT INTO refresh_tokens (user_id, jti, family_id, issued_at, expires_at, revoked_at, replaced_by_jti, ip, user_agent, created_at)
SELECT u.id, 'rt-jti-0001', 'rt-family-0001', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NULL, '127.0.0.1', 'seed-agent', NOW()
FROM app_user u
WHERE u.email = 'client1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM refresh_tokens r WHERE r.jti = 'rt-jti-0001');

INSERT INTO password_reset_tokens (user_id, token, expires_at, used, created_at)
SELECT u.id, 'reset-token-0001', DATE_ADD(NOW(), INTERVAL 1 HOUR), FALSE, NOW()
FROM app_user u
WHERE u.email = 'client1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM password_reset_tokens t WHERE t.token = 'reset-token-0001');

INSERT INTO audit_log (actor_id, actor_type, action, resource_type, resource_id, metadata, correlation_id, created_at)
SELECT u.id, 'USER', 'ORDER_CREATE', 'ORDER', 'ORD-2026-0001', '{"orderCode":"ORD-2026-0001"}', 'corr-0001', NOW()
FROM app_user u
WHERE u.email = 'client1@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM audit_log a WHERE a.correlation_id = 'corr-0001');

INSERT INTO idempotency_key (idem_key, scope, request_hash, response_code, response_body, status, expires_at, created_at)
SELECT 'checkout-0001', 'ORDER_CREATE', 'hash-0001', 200, '{"orderCode":"ORD-2026-0001"}', 'COMPLETED', DATE_ADD(NOW(), INTERVAL 1 DAY), NOW()
WHERE NOT EXISTS (SELECT 1 FROM idempotency_key k WHERE k.scope = 'ORDER_CREATE' AND k.idem_key = 'checkout-0001');

SET FOREIGN_KEY_CHECKS=1;
