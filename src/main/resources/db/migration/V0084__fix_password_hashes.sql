-- Fix password hashes for demo users that were inserted with plaintext passwords
-- BCrypt hash for 'Password123!'
-- This migration ensures all demo users can log in properly

UPDATE app_user 
SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE email LIKE '%@demo.local' 
  AND password_hash NOT LIKE '$2a$%';
