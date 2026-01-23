ALTER TABLE product ADD COLUMN original_price DECIMAL(15, 2) DEFAULT NULL;

-- Optional: Update existing data to have original_price = price + 20% (for demo purposes only)
-- UPDATE product SET original_price = price * 1.2 WHERE original_price IS NULL;
