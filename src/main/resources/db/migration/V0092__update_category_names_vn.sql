-- Update Category Names to Vietnamese
UPDATE category SET name = 'Điện tử & Phụ kiện' WHERE name = 'Electronics' OR name = 'Dien tu';
UPDATE category SET name = 'Máy tính & Laptop' WHERE name = 'Laptops' OR name = 'Laptop';
UPDATE category SET name = 'Thời trang Nam' WHERE name = 'Men Clothing' OR name = 'Quan ao nam';
UPDATE category SET name = 'Điện thoại & Tablet' WHERE name = 'Phones' OR name = 'Dien thoai' OR name = 'Tablets';
UPDATE category SET name = 'Thời trang Nữ' WHERE name = 'Women Clothing';
UPDATE category SET name = 'Thời trang chung' WHERE name = 'Thoi trang' OR name = 'Fashion' OR name = 'Clothing';
UPDATE category SET name = 'Giày dép' WHERE name = 'Shoes' OR name = 'Giay dep';
UPDATE category SET name = 'Nhà cửa & Đời sống' WHERE name = 'Home & Living' OR name = 'Home';
UPDATE category SET name = 'Sách & Văn phòng phẩm' WHERE name = 'Books';
UPDATE category SET name = 'Gaming Gear' WHERE name = 'Gaming';
UPDATE category SET name = 'Thể thao & Du lịch' WHERE name = 'Fitness';
UPDATE category SET name = 'Sắc đẹp & Sức khỏe' WHERE name = 'Beauty';

-- Remove duplicates if any (Optional, simple cleanup based on assumption)
-- For now just renaming makes them look correct.
