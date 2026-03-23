-- V10: Create orders table
-- Date: 2026-03-23
-- Purpose: Customer orders management

CREATE TABLE IF NOT EXISTS public.orders (
    id SERIAL PRIMARY KEY,

    -- Identifiers
    order_number VARCHAR(50) NOT NULL UNIQUE,

    -- References
    restaurant_id INTEGER NOT NULL,
    table_id INTEGER NOT NULL,
    customer_id INTEGER,

    -- Order Details
    order_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',

    -- Amounts
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12, 2) DEFAULT 0,
    tax_amount DECIMAL(12, 2) DEFAULT 0,
    total_amount DECIMAL(12, 2) DEFAULT 0,

    -- Notes
    notes TEXT,
    special_instructions TEXT,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_orders_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES public.restaurant(restro_id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_table FOREIGN KEY (table_id)
        REFERENCES public.tables(id) ON DELETE RESTRICT,
    CONSTRAINT check_order_subtotal CHECK (subtotal >= 0),
    CONSTRAINT check_order_discount CHECK (discount_amount >= 0),
    CONSTRAINT check_order_tax CHECK (tax_amount >= 0),
    CONSTRAINT check_order_total CHECK (total_amount >= 0)
);

-- Update table foreign key to reference orders
DO $$
BEGIN
    ALTER TABLE public.tables ADD CONSTRAINT fk_tables_order
        FOREIGN KEY (current_order_id) REFERENCES public.orders(id) ON DELETE SET NULL;
EXCEPTION WHEN duplicate_object THEN
    NULL;
END $$;-- Indexes
CREATE INDEX IF NOT EXISTS idx_orders_restaurant_status ON orders(restaurant_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_table_id ON orders(table_id);
CREATE INDEX IF NOT EXISTS idx_orders_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_orders_created ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_restaurant_created ON orders(restaurant_id, created_at);

