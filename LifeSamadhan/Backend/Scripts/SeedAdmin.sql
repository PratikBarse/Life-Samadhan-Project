-- Admin User Seeder Script
-- Username: Admin
-- Email: admin@lifesamadhan.com
-- Password: Admin@123 (hashed)
-- Role: ADMIN

-- Insert Admin User
-- Note: You'll need to hash the password using BCrypt before inserting
-- This is a template - run this after hashing the password properly

-- First, check if admin already exists
DELETE FROM Users WHERE Email = 'admin@lifesamadhan.com';

-- Insert new admin
-- The password 'Admin@123' needs to be hashed using BCrypt
-- For now, this is a placeholder. You should:
-- 1. Either register through the API endpoint
-- 2. Or hash the password using BCrypt and insert manually

-- Recommended approach: Use the registration endpoint with this data
/*
POST http://localhost:5055/api/auth/register
Content-Type: application/json

{
  "name": "Admin",
  "email": "admin@lifesamadhan.com",
  "mobile": "9999999999",
  "password": "Admin@123",
  "role": "ADMIN"
}
*/

-- Alternative: If you want to insert directly, use this SQL after getting BCrypt hash
-- Example with a pre-hashed password (you must generate this for 'Admin@123')
/*
INSERT INTO Users (Name, Email, Mobile, Password, Role, Status, CreatedAt)
VALUES (
    'Admin',
    'admin@lifesamadhan.com',
    '9999999999',
    '$2a$11$YourBCryptHashedPasswordHere', -- Replace with actual BCrypt hash of 'Admin@123'
    'ADMIN',
    'ACTIVE',
    GETDATE()
);
*/
