-- V8: Enhance food table
-- Date: 2026-03-23
-- Purpose: Add category, media, availability, and metadata fields

ALTER TABLE food ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE food ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE food ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
ALTER TABLE food ADD COLUMN IF NOT EXISTS is_available BOOLEAN DEFAULT TRUE;
ALTER TABLE food ADD COLUMN IF NOT EXISTS preparation_time INTEGER;
ALTER TABLE food ADD COLUMN IF NOT EXISTS allergens VARCHAR(500);
ALTER TABLE food ADD COLUMN IF NOT EXISTS calories INTEGER;
ALTER TABLE food ADD COLUMN IF NOT EXISTS is_vegetarian BOOLEAN DEFAULT FALSE;
ALTER TABLE food ADD COLUMN IF NOT EXISTS is_spicy BOOLEAN DEFAULT FALSE;

-- Add foreign key to categories (PostgreSQL-compatible)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_food_category' AND table_name = 'food'
    ) THEN
        ALTER TABLE food ADD CONSTRAINT fk_food_category
            FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Add check constraint for price (PostgreSQL-compatible)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'check_food_price' AND table_name = 'food'
    ) THEN
        ALTER TABLE food ADD CONSTRAINT check_food_price CHECK (price > 0);
    END IF;
END $$;

-- Add indexes for common queries
CREATE INDEX IF NOT EXISTS idx_food_restaurant_available ON food(restro_id, is_available);
CREATE INDEX IF NOT EXISTS idx_food_restaurant_category ON food(restro_id, category_id);
CREATE INDEX IF NOT EXISTS idx_food_name_search ON food(name);
CREATE INDEX IF NOT EXISTS idx_food_price ON food(price);
CREATE INDEX IF NOT EXISTS idx_food_is_vegetarian ON food(is_vegetarian);
CREATE INDEX IF NOT EXISTS idx_food_is_spicy ON food(is_spicy);
