-- V16: Add order_type field to orders table
-- Date: 2026-04-04
-- Purpose: Distinguish between OFFLINE (dine-in) and ONLINE (delivery/takeaway) orders

ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_type VARCHAR(20) NOT NULL DEFAULT 'OFFLINE';

-- Add check constraint
ALTER TABLE orders ADD CONSTRAINT IF NOT EXISTS check_order_type
    CHECK (order_type IN ('OFFLINE', 'ONLINE'));

-- Add index for filtering by order type
CREATE INDEX IF NOT EXISTS idx_orders_order_type ON orders(restaurant_id, order_type);

