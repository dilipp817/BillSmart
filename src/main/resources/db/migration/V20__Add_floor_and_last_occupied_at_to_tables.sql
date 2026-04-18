-- V22: Add floor and last_occupied_at to tables
-- Date: 2026-04-12
-- Purpose: Expose floor number in API responses for table-map UI;
--          track last time each table was occupied for turnover reporting.

-- floor column already exists from V7 (floor INTEGER DEFAULT 1).
-- Nothing to add for floor — we are just mapping it in the entity and DTO.

-- Add last_occupied_at: set when a table transitions from OCCUPIED → AVAILABLE.
ALTER TABLE tables
    ADD COLUMN IF NOT EXISTS last_occupied_at TIMESTAMP NULL;

COMMENT ON COLUMN tables.last_occupied_at IS
    'Timestamp of the most recent time this table was released (OCCUPIED → AVAILABLE). NULL if the table has never been occupied.';

