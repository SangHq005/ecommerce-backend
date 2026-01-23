-- Fix password hashes for admin and other seed users
-- BCrypt hash for 'Password123!' is $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG

UPDATE app_user 
SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE email IN (
    'admin@gmail.com',
    'seller1@gmail.com',
    'seller2@gmail.com',
    'client1@gmail.com',
    'client2@gmail.com'
);
