UPDATE product 
SET original_price = price + (price * 20 / 100) 
WHERE id <= 20 AND original_price IS NULL;
