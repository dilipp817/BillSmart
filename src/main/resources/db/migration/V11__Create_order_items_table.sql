-- V11: Create order_items table
-- Date: 2026-03-23
-- Purpose: Individual items within an order

CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,

    -- References
    order_id INTEGER NOT NULL,
    menu_item_id INTEGER NOT NULL,

    -- Quantity & Pricing
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,

    -- Item Details
    variant_id INTEGER,
    variant_name VARCHAR(255),
    special_instructions TEXT,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_food FOREIGN KEY (menu_item_id)
        REFERENCES food(id) ON DELETE RESTRICT,
    CONSTRAINT check_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT check_order_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT check_order_item_subtotal CHECK (subtotal >= 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_menu_item_id ON order_items(menu_item_id);

