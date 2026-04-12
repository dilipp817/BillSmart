-- V19: Update order_type values — OFFLINE → DINE_IN, ONLINE → TAKEAWAY, add DELIVERY
-- Date: 2026-04-12
-- Reason: ONLINE was ambiguous (covered both takeaway and delivery). Split into distinct values.

-- Step 1: Migrate existing rows
UPDATE orders SET order_type = 'DINE_IN'  WHERE order_type = 'OFFLINE';
UPDATE orders SET order_type = 'TAKEAWAY' WHERE order_type = 'ONLINE';

-- Step 2: Drop old CHECK constraint
ALTER TABLE orders DROP CONSTRAINT IF EXISTS check_order_type;

-- Step 3: Add updated CHECK constraint with 3 distinct values
ALTER TABLE orders ADD CONSTRAINT check_order_type
    CHECK (order_type IN ('DINE_IN', 'TAKEAWAY', 'DELIVERY'));

-- Step 4: Update column default
ALTER TABLE orders ALTER COLUMN order_type SET DEFAULT 'DINE_IN';

-- Update index comment (index itself is unchanged)
-- idx_orders_order_type covers (restaurant_id, order_type) — still valid

