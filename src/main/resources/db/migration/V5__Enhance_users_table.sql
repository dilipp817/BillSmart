-- V5: Enhance users table with authentication and authorization fields
-- Date: 2026-03-23

ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50) DEFAULT 'staff';
ALTER TABLE users ADD COLUMN IF NOT EXISTS restaurant_id INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS permissions TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS device_id VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS device_type VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;

-- Add foreign key constraint if it doesn't exist (H2-compatible)
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS fk_users_restaurant
    FOREIGN KEY (restaurant_id) REFERENCES restaurant(restro_id) ON DELETE SET NULL;

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_restaurant_id ON users(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
