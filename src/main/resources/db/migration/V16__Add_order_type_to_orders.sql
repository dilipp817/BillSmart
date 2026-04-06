-- V16: Add order_type field to orders table
-- Date: 2026-04-04
-- Purpose: Distinguish between OFFLINE (dine-in) and ONLINE (delivery/takeaway) orders

ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_type VARCHAR(20) NOT NULL DEFAULT 'OFFLINE';

-- Add check constraint (PostgreSQL-compatible)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'check_order_type' AND table_name = 'orders'
    ) THEN
        ALTER TABLE orders ADD CONSTRAINT check_order_type
            CHECK (order_type IN ('OFFLINE', 'ONLINE'));
    END IF;
END $$;

-- Add index for filtering by order type
CREATE INDEX IF NOT EXISTS idx_orders_order_type ON orders(restaurant_id, order_type);
