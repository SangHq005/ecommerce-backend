-- Force update password to plaintext 'Password123!' for admin
-- The PasswordHasher allows plaintext if it doesn't start with $2a$
-- This is a temporary measure to ensure login works

UPDATE app_user 
SET password_hash = 'Password123!'
WHERE email = 'admin@gmail.com';
