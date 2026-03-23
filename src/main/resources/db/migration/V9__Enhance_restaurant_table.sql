-- V9: Enhance restaurant table
-- Date: 2026-03-23
-- Purpose: Add tax configuration and branding fields

ALTER TABLE public.restaurant ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE public.restaurant ADD COLUMN IF NOT EXISTS tax_rate DECIMAL(5, 2) DEFAULT 18.00;
ALTER TABLE public.restaurant ADD COLUMN IF NOT EXISTS gstin VARCHAR(50);
ALTER TABLE public.restaurant ADD COLUMN IF NOT EXISTS pan_number VARCHAR(50);
ALTER TABLE public.restaurant ADD COLUMN IF NOT EXISTS store_logo_url VARCHAR(500);
ALTER TABLE public.restaurant ADD COLUMN IF NOT EXISTS store_logo_media_type VARCHAR(100);

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_restaurant_outlet_name ON public.restaurant(outlet_name);
CREATE INDEX IF NOT EXISTS idx_restaurant_is_active ON public.restaurant(is_active);
