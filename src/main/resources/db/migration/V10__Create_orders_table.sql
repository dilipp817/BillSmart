-- V10: Create orders table
-- Date: 2026-03-23
-- Purpose: Customer orders management (matches Order entity exactly)

CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,

    -- Identifiers
    order_number VARCHAR(50) NOT NULL UNIQUE,

    -- References
    restaurant_id INTEGER NOT NULL,
    table_id INTEGER NOT NULL,

    -- Order Details
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    -- Amounts
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,

    -- Notes
    notes TEXT,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT fk_orders_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES restaurant(restro_id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_table FOREIGN KEY (table_id)
        REFERENCES tables(id) ON DELETE RESTRICT,
    CONSTRAINT check_order_total CHECK (total_amount >= 0)
);

-- Update tables foreign key to reference orders (H2-compatible syntax)
ALTER TABLE tables ADD CONSTRAINT IF NOT EXISTS fk_tables_order
    FOREIGN KEY (current_order_id) REFERENCES orders(id) ON DELETE SET NULL;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_orders_restaurant ON orders(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_table ON orders(table_id);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
