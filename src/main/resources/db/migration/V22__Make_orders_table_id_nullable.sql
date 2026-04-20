-- V22: Make table_id nullable in orders to support TAKEAWAY orders
-- Date: 2026-04-21
-- Reason: TAKEAWAY orders have no table. Mobile sends table_id=null for these.
--         PostgreSQL FK constraints handle NULL gracefully — no FK check is applied on NULL.

ALTER TABLE orders ALTER COLUMN table_id DROP NOT NULL;

