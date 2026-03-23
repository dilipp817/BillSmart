-- V13: Create bill_items table
-- Date: 2026-03-23
-- Purpose: Items detail in a bill (copy from order_items for audit trail and printing)

CREATE TABLE IF NOT EXISTS bill_items (
    id SERIAL PRIMARY KEY,

    -- References
    bill_id INTEGER NOT NULL,
    order_item_id INTEGER,

    -- Item Details
    menu_item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,

    -- Variant Info
    variant_name VARCHAR(255),

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_bill_items_bill FOREIGN KEY (bill_id)
        REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_items_order_item FOREIGN KEY (order_item_id)
        REFERENCES order_items(id) ON DELETE SET NULL,
    CONSTRAINT check_bill_item_quantity CHECK (quantity > 0),
    CONSTRAINT check_bill_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT check_bill_item_subtotal CHECK (subtotal >= 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_bill_items_bill_id ON bill_items(bill_id);
CREATE INDEX IF NOT EXISTS idx_bill_items_order_item_id ON bill_items(order_item_id);

