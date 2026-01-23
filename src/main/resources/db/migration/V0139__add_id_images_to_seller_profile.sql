-- ============================================================================
-- V0139: Add ID Image Fields to Seller Profile
-- Add id_image_front and id_image_back columns to store uploaded ID card images
-- ============================================================================

ALTER TABLE seller_profile 
ADD COLUMN id_image_front VARCHAR(512) COMMENT 'URL to front side of ID card/CCCD',
ADD COLUMN id_image_back VARCHAR(512) COMMENT 'URL to back side of ID card/CCCD';
