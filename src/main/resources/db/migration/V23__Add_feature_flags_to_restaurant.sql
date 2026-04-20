-- V23: Add feature flag columns to restaurant table
-- Date: 2026-04-21
-- Reason: Each restaurant can independently configure their workflow.
--         is_table_management_enabled: false = counter-service / fast-food mode (no table selection).
--         is_pay_before_seat_enabled: true = payment collected upfront before seating (fast-casual dine-in).

ALTER TABLE restaurant
    ADD COLUMN IF NOT EXISTS is_table_management_enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE restaurant
    ADD COLUMN IF NOT EXISTS is_pay_before_seat_enabled BOOLEAN NOT NULL DEFAULT FALSE;

