-- V8: Enhance food table
-- Date: 2026-03-23
-- Purpose: Add category, media, availability, and metadata fields

ALTER TABLE food ADD COLUMN IF NOT EXISTS category_id INTEGER;
ALTER TABLE food ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE food ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
ALTER TABLE food ADD COLUMN IF NOT EXISTS is_available BOOLEAN DEFAULT TRUE;
ALTER TABLE food ADD COLUMN IF NOT EXISTS preparation_time INTEGER;
ALTER TABLE food ADD COLUMN IF NOT EXISTS allergens VARCHAR(500);
ALTER TABLE food ADD COLUMN IF NOT EXISTS calories INTEGER;
ALTER TABLE food ADD COLUMN IF NOT EXISTS is_vegetarian BOOLEAN DEFAULT FALSE;
ALTER TABLE food ADD COLUMN IF NOT EXISTS is_spicy BOOLEAN DEFAULT FALSE;

-- Add foreign key to categories (H2-compatible)
ALTER TABLE food ADD CONSTRAINT IF NOT EXISTS fk_food_category
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;

-- Add check constraint for price (H2-compatible)
ALTER TABLE food ADD CONSTRAINT IF NOT EXISTS check_food_price CHECK (price > 0);

-- Add indexes for common queries
CREATE INDEX IF NOT EXISTS idx_food_restaurant_available ON food(restro_id, is_available);
CREATE INDEX IF NOT EXISTS idx_food_restaurant_category ON food(restro_id, category_id);
CREATE INDEX IF NOT EXISTS idx_food_name_search ON food(name);
CREATE INDEX IF NOT EXISTS idx_food_price ON food(price);
CREATE INDEX IF NOT EXISTS idx_food_is_vegetarian ON food(is_vegetarian);
CREATE INDEX IF NOT EXISTS idx_food_is_spicy ON food(is_spicy);
