-- Add Shopee-like onboarding fields to seller_shop
ALTER TABLE seller_shop 
ADD COLUMN contact_name VARCHAR(100),
ADD COLUMN contact_phone VARCHAR(20),
ADD COLUMN contact_email VARCHAR(100),
ADD COLUMN identity_code VARCHAR(50),
ADD COLUMN tax_code VARCHAR(50),
ADD COLUMN bank_name VARCHAR(100),
ADD COLUMN bank_account_number VARCHAR(50),
ADD COLUMN bank_account_name VARCHAR(100);
