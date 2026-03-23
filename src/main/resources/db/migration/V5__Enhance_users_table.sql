-- V5: Enhance users table with authentication and authorization fields
-- Date: 2026-03-23

-- Enhance existing users table with authentication, authorization, and device tracking
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS password VARCHAR(500);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS role VARCHAR(50) DEFAULT 'staff';
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS restaurant_id INTEGER;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS permissions TEXT;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS device_id VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS device_type VARCHAR(50);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    ALTER TABLE public.users ADD CONSTRAINT fk_users_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES public.restaurant(restro_id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_restaurant_id ON public.users(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON public.users(is_active);

