-- ============================================================================
-- Quick Reference Queries for Test Data
-- ============================================================================
-- This file contains useful queries to explore and verify the test data

-- ============================================================================
-- USER QUERIES
-- ============================================================================

-- List all users with their roles
SELECT 
    u.id,
    u.email,
    u.full_name,
    u.status,
    GROUP_CONCAT(r.code) as roles,
    u.created_at
FROM app_user u
LEFT JOIN user_role ur ON u.id = ur.user_id
LEFT JOIN role r ON ur.role_id = r.id
GROUP BY u.id, u.email, u.full_name, u.status, u.created_at
ORDER BY u.id;

-- List all customers with profiles
SELECT 
    u.id,
    u.email,
    u.full_name,
    up.phone,
    up.gender,
    up.date_of_birth,
    u.status
FROM app_user u
JOIN user_role ur ON u.id = ur.user_id
JOIN role r ON ur.role_id = r.id
LEFT JOIN user_profile up ON u.id = up.user_id
WHERE r.code = 'CLIENT'
ORDER BY u.id;

-- List all sellers with their shops
SELECT 
    u.id as user_id,
    u.email,
    u.full_name,
    ss.id as shop_id,
    ss.shop_name,
    ss.shop_slug,
    ss.status as shop_status,
    ss.verified_at
FROM app_user u
JOIN user_role ur ON u.id = ur.user_id
JOIN role r ON ur.role_id = r.id
LEFT JOIN seller_shop ss ON u.id = ss.seller_user_id
WHERE r.code = 'SELLER'
ORDER BY u.id;

-- ============================================================================
-- PRODUCT QUERIES
-- ============================================================================

-- List all products with shop and category info
SELECT 
    p.id,
    p.name,
    p.slug,
    ss.shop_name,
    c.name as category,
    b.name as brand,
    p.price,
    p.stock_quantity,
    p.status,
    p.average_rating,
    p.review_count
FROM product p
JOIN seller_shop ss ON p.shop_id = ss.id
JOIN category c ON p.category_id = c.id
LEFT JOIN brand b ON p.brand_id = b.id
ORDER BY ss.shop_name, p.name;

-- Products by category
SELECT 
    c.name as category,
    COUNT(p.id) as product_count,
    AVG(p.price) as avg_price,
    SUM(p.stock_quantity) as total_stock
FROM category c
LEFT JOIN product p ON c.id = p.category_id AND p.status = 'ACTIVE'
GROUP BY c.id, c.name
ORDER BY product_count DESC;

-- Products by brand
SELECT 
    b.name as brand,
    COUNT(p.id) as product_count,
    MIN(p.price) as min_price,
    MAX(p.price) as max_price,
    AVG(p.price) as avg_price
FROM brand b
LEFT JOIN product p ON b.id = p.brand_id AND p.status = 'ACTIVE'
GROUP BY b.id, b.name
ORDER BY product_count DESC;

-- Products with SKU details
SELECT 
    p.id,
    p.name,
    ps.sku_code,
    ps.price,
    ps.stock_on_hand,
    ps.reserved_stock,
    (ps.stock_on_hand - ps.reserved_stock) as available_stock
FROM product p
JOIN product_sku ps ON p.id = ps.product_id
ORDER BY p.name, ps.sku_code;

-- Products with images
SELECT 
    p.id,
    p.name,
    COUNT(pi.id) as image_count,
    GROUP_CONCAT(pi.image_url ORDER BY pi.sort_order SEPARATOR '\n') as images
FROM product p
LEFT JOIN product_image pi ON p.id = pi.product_id
GROUP BY p.id, p.name
ORDER BY p.name;

-- Top rated products
SELECT 
    p.id,
    p.name,
    ss.shop_name,
    p.average_rating,
    p.review_count,
    p.price
FROM product p
JOIN seller_shop ss ON p.shop_id = ss.id
WHERE p.review_count > 0
ORDER BY p.average_rating DESC, p.review_count DESC
LIMIT 10;

-- ============================================================================
-- ORDER QUERIES
-- ============================================================================

-- List all orders with customer and shop info
SELECT 
    o.id,
    o.order_code,
    u.email as customer_email,
    u.full_name as customer_name,
    ss.shop_name,
    o.status,
    o.total_amount,
    o.currency,
    o.created_at
FROM orders o
JOIN app_user u ON o.user_id = u.id
JOIN seller_shop ss ON o.shop_id = ss.id
ORDER BY o.created_at DESC;

-- Order details with items
SELECT 
    o.order_code,
    u.email as customer,
    ss.shop_name,
    p.name as product,
    ps.sku_code,
    oi.quantity,
    oi.unit_price,
    oi.total_price,
    o.status as order_status
FROM orders o
JOIN app_user u ON o.user_id = u.id
JOIN seller_shop ss ON o.shop_id = ss.id
JOIN order_item oi ON o.id = oi.order_id
JOIN product p ON oi.product_id = p.id
JOIN product_sku ps ON oi.sku_id = ps.id
ORDER BY o.created_at DESC, oi.id;

-- Orders by status
SELECT 
    status,
    COUNT(*) as order_count,
    SUM(total_amount) as total_revenue,
    AVG(total_amount) as avg_order_value
FROM orders
GROUP BY status
ORDER BY order_count DESC;

-- Orders by shop
SELECT 
    ss.shop_name,
    COUNT(o.id) as order_count,
    SUM(o.total_amount) as total_revenue,
    AVG(o.total_amount) as avg_order_value
FROM seller_shop ss
LEFT JOIN orders o ON ss.id = o.shop_id
GROUP BY ss.id, ss.shop_name
ORDER BY total_revenue DESC;

-- Recent orders (last 7 days)
SELECT 
    o.order_code,
    u.email,
    ss.shop_name,
    o.status,
    o.total_amount,
    o.created_at
FROM orders o
JOIN app_user u ON o.user_id = u.id
JOIN seller_shop ss ON o.shop_id = ss.id
WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY o.created_at DESC;

-- ============================================================================
-- PAYMENT QUERIES
-- ============================================================================

-- List all payments
SELECT 
    p.id,
    o.order_code,
    p.amount,
    p.currency,
    p.method,
    p.status,
    p.transaction_id,
    p.gateway,
    p.created_at
FROM payments p
JOIN orders o ON p.order_id = o.id
ORDER BY p.created_at DESC;

-- Payment summary by method
SELECT 
    method,
    COUNT(*) as payment_count,
    SUM(amount) as total_amount,
    AVG(amount) as avg_amount
FROM payments
WHERE status = 'COMPLETED'
GROUP BY method
ORDER BY total_amount DESC;

-- ============================================================================
-- CART QUERIES
-- ============================================================================

-- Current cart items
SELECT 
    u.email,
    ss.shop_name,
    p.name as product,
    ps.sku_code,
    ci.quantity,
    ps.price,
    (ci.quantity * ps.price) as subtotal
FROM cart_item ci
JOIN app_user u ON ci.user_id = u.id
JOIN seller_shop ss ON ci.shop_id = ss.id
JOIN product p ON ci.product_id = p.id
JOIN product_sku ps ON ci.sku_id = ps.id
ORDER BY u.email, ss.shop_name;

-- Cart summary by user
SELECT 
    u.email,
    COUNT(ci.id) as item_count,
    SUM(ci.quantity * ps.price) as cart_total
FROM app_user u
LEFT JOIN cart_item ci ON u.id = ci.user_id
LEFT JOIN product_sku ps ON ci.sku_id = ps.id
GROUP BY u.id, u.email
HAVING item_count > 0
ORDER BY cart_total DESC;

-- ============================================================================
-- REVIEW QUERIES
-- ============================================================================

-- All reviews with product and user info
SELECT 
    r.id,
    p.name as product,
    u.email as reviewer,
    r.rating,
    r.comment,
    r.status,
    r.helpful_count,
    r.created_at
FROM review r
JOIN product p ON r.product_id = p.id
JOIN app_user u ON r.user_id = u.id
ORDER BY r.created_at DESC;

-- Average rating by product
SELECT 
    p.name,
    COUNT(r.id) as review_count,
    AVG(r.rating) as avg_rating,
    MIN(r.rating) as min_rating,
    MAX(r.rating) as max_rating
FROM product p
LEFT JOIN review r ON p.id = r.product_id AND r.status = 'APPROVED'
GROUP BY p.id, p.name
HAVING review_count > 0
ORDER BY avg_rating DESC, review_count DESC;

-- Reviews by rating
SELECT 
    rating,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM review), 2) as percentage
FROM review
WHERE status = 'APPROVED'
GROUP BY rating
ORDER BY rating DESC;

-- ============================================================================
-- WISHLIST QUERIES
-- ============================================================================

-- Wishlist items
SELECT 
    u.email,
    p.name as product,
    p.price,
    wi.note,
    wi.added_at
FROM wishlist_item wi
JOIN app_user u ON wi.user_id = u.id
JOIN product p ON wi.product_id = p.id
ORDER BY u.email, wi.added_at DESC;

-- Most wishlisted products
SELECT 
    p.name,
    COUNT(wi.id) as wishlist_count,
    p.price
FROM product p
JOIN wishlist_item wi ON p.id = wi.product_id
GROUP BY p.id, p.name, p.price
ORDER BY wishlist_count DESC
LIMIT 10;

-- ============================================================================
-- COUPON QUERIES
-- ============================================================================

-- Active coupons
SELECT 
    code,
    name,
    type,
    discount_value,
    max_discount_amount,
    min_order_amount,
    usage_limit,
    usage_count,
    (usage_limit - usage_count) as remaining_uses,
    start_date,
    end_date,
    status
FROM coupon
WHERE status = 'ACTIVE'
  AND start_date <= NOW()
  AND end_date >= NOW()
ORDER BY discount_value DESC;

-- Coupon usage statistics
SELECT 
    c.code,
    c.name,
    COUNT(cu.id) as times_used,
    SUM(cu.discount_amount) as total_discount_given,
    AVG(cu.discount_amount) as avg_discount
FROM coupon c
LEFT JOIN coupon_usage cu ON c.id = cu.coupon_id
GROUP BY c.id, c.code, c.name
ORDER BY times_used DESC;

-- ============================================================================
-- STOCK QUERIES
-- ============================================================================

-- Stock levels by product
SELECT 
    p.name,
    ps.sku_code,
    ps.stock_on_hand,
    ps.reserved_stock,
    (ps.stock_on_hand - ps.reserved_stock) as available_stock,
    CASE 
        WHEN (ps.stock_on_hand - ps.reserved_stock) = 0 THEN 'OUT_OF_STOCK'
        WHEN (ps.stock_on_hand - ps.reserved_stock) < 10 THEN 'LOW_STOCK'
        ELSE 'IN_STOCK'
    END as stock_status
FROM product p
JOIN product_sku ps ON p.id = ps.product_id
ORDER BY available_stock ASC;

-- Stock movements history
SELECT 
    ps.sku_code,
    sm.delta,
    sm.reason,
    u.email as actor,
    sm.created_at
FROM stock_movement sm
JOIN product_sku ps ON sm.sku_id = ps.id
LEFT JOIN app_user u ON sm.actor_id = u.id
ORDER BY sm.created_at DESC
LIMIT 50;

-- Active stock reservations
SELECT 
    sr.order_token,
    ps.sku_code,
    p.name as product,
    sr.qty,
    sr.status,
    sr.created_at,
    sr.expires_at,
    CASE 
        WHEN sr.expires_at < NOW() THEN 'EXPIRED'
        ELSE 'ACTIVE'
    END as reservation_status
FROM stock_reservation sr
JOIN product_sku ps ON sr.sku_id = ps.id
JOIN product p ON ps.product_id = p.id
WHERE sr.status = 'RESERVED'
ORDER BY sr.created_at DESC;

-- ============================================================================
-- CATEGORY QUERIES
-- ============================================================================

-- Category hierarchy
SELECT 
    c1.id,
    c1.name as category,
    c2.name as parent_category,
    c1.path,
    c1.is_active,
    c1.sort_order
FROM category c1
LEFT JOIN category c2 ON c1.parent_id = c2.id
ORDER BY c1.path;

-- Category tree with product counts
SELECT 
    c.id,
    c.name,
    c.parent_id,
    COUNT(p.id) as product_count,
    c.is_active
FROM category c
LEFT JOIN product p ON c.id = p.category_id AND p.status = 'ACTIVE'
GROUP BY c.id, c.name, c.parent_id, c.is_active
ORDER BY c.path;

-- ============================================================================
-- ANALYTICS QUERIES
-- ============================================================================

-- Sales summary
SELECT 
    COUNT(DISTINCT o.id) as total_orders,
    COUNT(DISTINCT o.user_id) as unique_customers,
    SUM(o.total_amount) as total_revenue,
    AVG(o.total_amount) as avg_order_value,
    MIN(o.total_amount) as min_order_value,
    MAX(o.total_amount) as max_order_value
FROM orders o
WHERE o.status IN ('PAID', 'SHIPPED', 'DELIVERED');

-- Top customers by order value
SELECT 
    u.email,
    u.full_name,
    COUNT(o.id) as order_count,
    SUM(o.total_amount) as total_spent,
    AVG(o.total_amount) as avg_order_value
FROM app_user u
JOIN orders o ON u.id = o.user_id
WHERE o.status IN ('PAID', 'SHIPPED', 'DELIVERED')
GROUP BY u.id, u.email, u.full_name
ORDER BY total_spent DESC
LIMIT 10;

-- Best selling products
SELECT 
    p.name,
    ss.shop_name,
    SUM(oi.quantity) as units_sold,
    SUM(oi.total_price) as revenue,
    AVG(oi.unit_price) as avg_price
FROM product p
JOIN seller_shop ss ON p.shop_id = ss.id
JOIN order_item oi ON p.id = oi.product_id
JOIN orders o ON oi.order_id = o.id
WHERE o.status IN ('PAID', 'SHIPPED', 'DELIVERED')
GROUP BY p.id, p.name, ss.shop_name
ORDER BY units_sold DESC
LIMIT 10;

-- Shop performance
SELECT 
    ss.shop_name,
    ss.status,
    COUNT(DISTINCT p.id) as product_count,
    COUNT(DISTINCT o.id) as order_count,
    COALESCE(SUM(o.total_amount), 0) as total_revenue,
    COALESCE(AVG(o.total_amount), 0) as avg_order_value
FROM seller_shop ss
LEFT JOIN product p ON ss.id = p.shop_id
LEFT JOIN orders o ON ss.id = o.shop_id AND o.status IN ('PAID', 'SHIPPED', 'DELIVERED')
GROUP BY ss.id, ss.shop_name, ss.status
ORDER BY total_revenue DESC;

-- ============================================================================
-- DATA INTEGRITY CHECKS
-- ============================================================================

-- Check for products without SKUs
SELECT p.id, p.name, p.slug
FROM product p
LEFT JOIN product_sku ps ON p.id = ps.product_id
WHERE ps.id IS NULL;

-- Check for orders without items
SELECT o.id, o.order_code, o.status
FROM orders o
LEFT JOIN order_item oi ON o.id = oi.order_id
WHERE oi.id IS NULL;

-- Check for users without roles
SELECT u.id, u.email, u.full_name
FROM app_user u
LEFT JOIN user_role ur ON u.id = ur.user_id
WHERE ur.user_id IS NULL;

-- Check for sellers without shops
SELECT u.id, u.email, u.full_name
FROM app_user u
JOIN user_role ur ON u.id = ur.user_id
JOIN role r ON ur.role_id = r.id
LEFT JOIN seller_shop ss ON u.id = ss.seller_user_id
WHERE r.code = 'SELLER' AND ss.id IS NULL;

-- Check for negative stock
SELECT p.name, ps.sku_code, ps.stock_on_hand, ps.reserved_stock
FROM product_sku ps
JOIN product p ON ps.product_id = p.id
WHERE ps.stock_on_hand < 0 OR ps.reserved_stock < 0;

-- Check for expired reservations
SELECT 
    sr.order_token,
    ps.sku_code,
    sr.qty,
    sr.status,
    sr.expires_at
FROM stock_reservation sr
JOIN product_sku ps ON sr.sku_id = ps.id
WHERE sr.status = 'RESERVED' AND sr.expires_at < NOW();
