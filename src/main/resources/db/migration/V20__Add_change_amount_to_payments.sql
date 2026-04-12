-- V20: Add change_amount column to payments table
-- Date: 2026-04-12
-- Reason: Cash POS needs to record change returned to customer (amount_tendered - bill_total)

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS change_amount DECIMAL(12, 2) NOT NULL DEFAULT 0;

ALTER TABLE payments
    ADD CONSTRAINT check_change_amount CHECK (change_amount >= 0);

