-- Add city column to seller_shop table
ALTER TABLE seller_shop ADD COLUMN city VARCHAR(100) NULL AFTER status;

-- Update existing shops with some sample cities for variety
UPDATE seller_shop SET city = 'TP. Hồ Chí Minh' WHERE id % 5 = 0;
UPDATE seller_shop SET city = 'Hà Nội' WHERE id % 5 = 1;
UPDATE seller_shop SET city = 'Đà Nẵng' WHERE id % 5 = 2;
UPDATE seller_shop SET city = 'Cần Thơ' WHERE id % 5 = 3;
UPDATE seller_shop SET city = 'Hải Phòng' WHERE id % 5 = 4;
