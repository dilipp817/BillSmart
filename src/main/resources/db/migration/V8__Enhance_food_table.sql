-- V8: Enhance food table
-- Date: 2026-03-23
-- Purpose: Add category, media, availability, and metadata fields

-- Add columns to existing food table
ALTER TABLE public.food ADD COLUMN IF NOT EXISTS category_id INTEGER;
ALTER TABLE public.food ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE public.food ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
ALTER TABLE public.food ADD COLUMN IF NOT EXISTS is_available BOOLEAN DEFAULT TRUE;
ALTER TABLE public.food ADD COLUMN IF NOT EXISTS preparation_time INTEGER;
ALTER TABLE public.food ADD COLUMN IF NOT EXISTS allergens VARCHAR(500);
ALTER TABLE public.food ADD COLUMN IF NOT EXISTS calories INTEGER;
ALTER TABLE public.food ADD COLUMN IF NOT EXISTS is_vegetarian BOOLEAN DEFAULT FALSE;
ALTER TABLE public.food ADD COLUMN IF NOT EXISTS is_spicy BOOLEAN DEFAULT FALSE;

-- Add foreign key to categories
DO $$
BEGIN
    ALTER TABLE public.food ADD CONSTRAINT fk_food_category
        FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;

-- Add check constraint for price
DO $$
BEGIN
    ALTER TABLE public.food ADD CONSTRAINT check_food_price CHECK (price > 0);
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;

-- Add indexes for common queries
CREATE INDEX IF NOT EXISTS idx_food_restaurant_available ON public.food(restro_id, is_available);
CREATE INDEX IF NOT EXISTS idx_food_restaurant_category ON public.food(restro_id, category_id);
CREATE INDEX IF NOT EXISTS idx_food_name_search ON public.food(name);
CREATE INDEX IF NOT EXISTS idx_food_price ON public.food(price);
CREATE INDEX IF NOT EXISTS idx_food_is_vegetarian ON public.food(is_vegetarian);
CREATE INDEX IF NOT EXISTS idx_food_is_spicy ON public.food(is_spicy);
