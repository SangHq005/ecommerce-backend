-- ================================
-- Performance Indexes Migration
-- ================================
-- This migration adds indexes to frequently queried columns
-- to improve query performance across the application

-- PRODUCTS TABLE
-- Index for category filtering
CREATE INDEX idx_product_category_status
ON product(category_id, status);

-- Index for brand filtering
CREATE INDEX idx_product_brand_status
ON product(brand_id, status);

-- Index for price range queries
CREATE INDEX idx_product_price
ON product(price);

-- Index for search by name
CREATE INDEX idx_product_name
ON product(name);

-- Index for sorting by created date
CREATE INDEX idx_product_created
ON product(created_at DESC);

-- Index for rating queries
CREATE INDEX idx_product_rating
ON product(average_rating DESC);

-- ORDERS TABLE
-- Index for user's orders
CREATE INDEX idx_order_user_created
ON orders(user_id, created_at DESC);

-- Index for shop's orders
CREATE INDEX idx_order_shop_status
ON orders(shop_id, status, created_at DESC);

-- Index for order status queries
CREATE INDEX idx_order_status_created
ON orders(status, created_at DESC);

-- Index for order code lookup
CREATE INDEX idx_order_code
ON orders(order_code);

-- REVIEWS TABLE
-- Index for product reviews
CREATE INDEX idx_review_product_status
ON review(product_id, status, created_at DESC);

-- Index for user reviews
CREATE INDEX idx_review_user_date
ON review(user_id, created_at DESC);

-- Index for pending reviews (admin moderation)
CREATE INDEX idx_review_status_created
ON review(status, created_at);

-- CART ITEMS TABLE
-- Index for user cart
CREATE INDEX idx_cart_user_created
ON cart_item(user_id, created_at DESC);

-- PAYMENTS TABLE
-- Index for order payments
CREATE INDEX idx_payment_order
ON payments(order_id);

-- Index for payment status
CREATE INDEX idx_payment_status_created
ON payments(status, created_at DESC);

-- Index for transaction ID lookup
CREATE INDEX idx_payment_transaction
ON payments(transaction_id);

-- COUPONS TABLE
-- Index for active coupons
CREATE INDEX idx_coupon_code_active
ON coupon(code, status);



-- NOTIFICATIONS TABLE
-- Index for user notifications
CREATE INDEX idx_notification_user_read
ON notification(user_id, is_read, created_at DESC);

-- WISHLIST TABLE
-- Index for user wishlist
CREATE INDEX idx_wishlist_user_product
ON wishlist_item(user_id, product_id);

-- REFUNDS TABLE
-- Index for order refunds
CREATE INDEX idx_refund_order
ON refund(order_id);

-- Index for user refunds
CREATE INDEX idx_refund_user_status
ON refund(user_id, status, created_at DESC);

-- PRODUCT_SKUS TABLE


-- USER_ADDRESSES TABLE
-- Index for user addresses
CREATE INDEX idx_address_user_default
ON user_address(user_id, is_default);

-- ================================
-- COMPOSITE INDEXES FOR COMMON QUERIES
-- ================================

-- Products by category, price range, and status
CREATE INDEX idx_product_category_price_status
ON product(category_id, price, status);

-- Orders by user, status, and date
CREATE INDEX idx_order_user_status_date
ON orders(user_id, status, created_at DESC);

-- Reviews by product and rating
CREATE INDEX idx_review_product_rating
ON review(product_id, rating, created_at DESC);

-- ================================
-- ANALYZE TABLES FOR QUERY PLANNER
-- ================================
ANALYZE TABLE product;
ANALYZE TABLE orders;
ANALYZE TABLE review;
ANALYZE TABLE cart_item;
ANALYZE TABLE payments;
ANALYZE TABLE coupon;
ANALYZE TABLE notification;
