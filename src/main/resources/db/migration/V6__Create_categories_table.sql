-- V6: Create categories table
-- Date: 2026-03-23
-- Purpose: Menu organization and categorization

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,

    -- Reference
    restaurant_id INTEGER NOT NULL,

    -- Content
    name VARCHAR(100) NOT NULL,
    description TEXT,

    -- Display
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    image_url VARCHAR(500),

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_categories_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES restaurant(restro_id) ON DELETE CASCADE,
    CONSTRAINT uk_categories_name_per_restaurant UNIQUE (restaurant_id, name)
);

-- Indexes
CREATE INDEX idx_categories_restaurant_id ON categories(restaurant_id);
CREATE INDEX idx_categories_restaurant_active ON categories(restaurant_id, is_active);
CREATE INDEX idx_categories_restaurant_order ON categories(restaurant_id, display_order);

