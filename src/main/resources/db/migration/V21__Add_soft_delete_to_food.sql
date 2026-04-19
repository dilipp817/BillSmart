-- V21: Add soft delete support to food table
-- Date: 2026-04-20
-- Purpose: Allow menu items to be soft-deleted (hidden from APIs) while preserving
--          historical references from order_items. Also adds updated_at for audit trail.
ALTER TABLE food ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE food ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE food ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
-- Index to make "WHERE is_deleted = FALSE" filters fast
CREATE INDEX IF NOT EXISTS idx_food_is_deleted ON food(is_deleted);
CREATE INDEX IF NOT EXISTS idx_food_restro_not_deleted ON food(restro_id, is_deleted);
