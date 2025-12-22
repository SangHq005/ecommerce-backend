-- ================================
-- Performance Indexes Migration
-- ================================
-- This migration adds indexes to frequently queried columns
-- to improve query performance across the application

-- PRODUCTS TABLE
-- Index for category filtering
CREATE INDEX IF NOT EXISTS idx_product_category_status
ON products(category_id, status);

-- Index for brand filtering
CREATE INDEX IF NOT EXISTS idx_product_brand_status
ON products(brand_id, status);

-- Index for price range queries
CREATE INDEX IF NOT EXISTS idx_product_price
ON products(price);

-- Index for search by name
CREATE INDEX IF NOT EXISTS idx_product_name
ON products(name);

-- Index for sorting by created date
CREATE INDEX IF NOT EXISTS idx_product_created
ON products(created_at DESC);

-- Index for rating queries
CREATE INDEX IF NOT EXISTS idx_product_rating
ON products(average_rating DESC);

-- ORDERS TABLE
-- Index for user's orders
CREATE INDEX IF NOT EXISTS idx_order_user_created
ON orders(user_id, created_at DESC);

-- Index for shop's orders
CREATE INDEX IF NOT EXISTS idx_order_shop_status
ON orders(shop_id, status, created_at DESC);

-- Index for order status queries
CREATE INDEX IF NOT EXISTS idx_order_status_created
ON orders(status, created_at DESC);

-- Index for order code lookup
CREATE INDEX IF NOT EXISTS idx_order_code
ON orders(order_code);

-- REVIEWS TABLE
-- Index for product reviews
CREATE INDEX IF NOT EXISTS idx_review_product_status
ON reviews(product_id, status, created_at DESC);

-- Index for user reviews
CREATE INDEX IF NOT EXISTS idx_review_user
ON reviews(user_id, created_at DESC);

-- Index for pending reviews (admin moderation)
CREATE INDEX IF NOT EXISTS idx_review_status_created
ON reviews(status, created_at);

-- CART ITEMS TABLE
-- Index for user cart
CREATE INDEX IF NOT EXISTS idx_cart_user_created
ON cart_items(user_id, created_at DESC);

-- PAYMENTS TABLE
-- Index for order payments
CREATE INDEX IF NOT EXISTS idx_payment_order
ON payments(order_id);

-- Index for payment status
CREATE INDEX IF NOT EXISTS idx_payment_status_created
ON payments(status, created_at DESC);

-- Index for transaction ID lookup
CREATE INDEX IF NOT EXISTS idx_payment_transaction
ON payments(transaction_id);

-- COUPONS TABLE
-- Index for active coupons
CREATE INDEX IF NOT EXISTS idx_coupon_code_active
ON coupons(code, status);

-- Index for validity period
CREATE INDEX IF NOT EXISTS idx_coupon_dates
ON coupons(start_date, end_date);

-- NOTIFICATIONS TABLE
-- Index for user notifications
CREATE INDEX IF NOT EXISTS idx_notification_user_read
ON notifications(user_id, is_read, created_at DESC);

-- WISHLIST TABLE
-- Index for user wishlist
CREATE INDEX IF NOT EXISTS idx_wishlist_user_product
ON wishlist_items(user_id, product_id);

-- REFUNDS TABLE
-- Index for order refunds
CREATE INDEX IF NOT EXISTS idx_refund_order
ON refunds(order_id);

-- Index for user refunds
CREATE INDEX IF NOT EXISTS idx_refund_user_status
ON refunds(user_id, status, created_at DESC);

-- PRODUCT_SKUS TABLE
-- Index for product variants
CREATE INDEX IF NOT EXISTS idx_sku_product
ON product_skus(product_id);

-- USER_ADDRESSES TABLE
-- Index for user addresses
CREATE INDEX IF NOT EXISTS idx_address_user_default
ON user_addresses(user_id, is_default);

-- ================================
-- COMPOSITE INDEXES FOR COMMON QUERIES
-- ================================

-- Products by category, price range, and status
CREATE INDEX IF NOT EXISTS idx_product_category_price_status
ON products(category_id, price, status);

-- Orders by user, status, and date
CREATE INDEX IF NOT EXISTS idx_order_user_status_date
ON orders(user_id, status, created_at DESC);

-- Reviews by product and rating
CREATE INDEX IF NOT EXISTS idx_review_product_rating
ON reviews(product_id, rating, created_at DESC);

-- ================================
-- ANALYZE TABLES FOR QUERY PLANNER
-- ================================
ANALYZE TABLE products;
ANALYZE TABLE orders;
ANALYZE TABLE reviews;
ANALYZE TABLE cart_items;
ANALYZE TABLE payments;
ANALYZE TABLE coupons;
ANALYZE TABLE notifications;
