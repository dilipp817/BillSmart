-- V13: Create bill_items table
-- Date: 2026-03-23
-- Purpose: Line items in a bill (matches BillItem entity exactly)

CREATE TABLE IF NOT EXISTS bill_items (
    id BIGSERIAL PRIMARY KEY,

    -- References
    bill_id BIGINT NOT NULL,
    food_id BIGINT,

    -- Item Details
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    item_total DECIMAL(10, 2) NOT NULL DEFAULT 0,

    -- Optimistic locking
    version BIGINT NOT NULL DEFAULT 0,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_bill_items_bill FOREIGN KEY (bill_id)
        REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_items_food FOREIGN KEY (food_id)
        REFERENCES food(id) ON DELETE SET NULL,
    CONSTRAINT check_bill_item_quantity CHECK (quantity > 0),
    CONSTRAINT check_bill_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT check_bill_item_total CHECK (item_total >= 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_bill_items_bill_id ON bill_items(bill_id);
CREATE INDEX IF NOT EXISTS idx_bill_items_food_id ON bill_items(food_id);
