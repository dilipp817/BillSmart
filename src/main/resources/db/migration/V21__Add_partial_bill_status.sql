-- V21: Add PARTIAL to the allowed bill status values
-- Date: 2026-04-12
-- Reason: Support partial payments — bill transitions to PARTIAL when a payment covers
--         less than the total amount, and then to PAID once fully settled.

-- Drop any pre-existing status constraint (none existed before, but guard against re-runs)
ALTER TABLE bills DROP CONSTRAINT IF EXISTS check_bill_status;

-- Add constraint that permits the new PARTIAL status
ALTER TABLE bills ADD CONSTRAINT check_bill_status
    CHECK (status IN ('ISSUED', 'PARTIAL', 'PAID', 'CANCELLED'));

