-- V11: Create order_items table
-- Date: 2026-03-23
-- Purpose: Individual items within an order (matches OrderItem entity exactly)

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,

    -- References
    order_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,

    -- Quantity & Pricing
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0,

    -- Special requests
    special_requests TEXT,

    -- Item status
    item_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_food FOREIGN KEY (food_id)
        REFERENCES food(id) ON DELETE RESTRICT,
    CONSTRAINT check_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT check_order_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT check_order_item_subtotal CHECK (subtotal >= 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_food ON order_items(food_id);
