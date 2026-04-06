-- V7: Create tables table
-- Date: 2026-03-23
-- Purpose: Restaurant seating management

CREATE TABLE tables (
    id BIGSERIAL PRIMARY KEY,

    -- Reference
    restaurant_id BIGINT NOT NULL,

    -- Table Info
    table_number VARCHAR(50) NOT NULL,
    floor INTEGER DEFAULT 1,
    capacity INTEGER NOT NULL,

    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'available',

    -- Current Order
    current_order_id BIGINT,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Optimistic locking
    version BIGINT NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT fk_tables_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES restaurant(restro_id) ON DELETE CASCADE,
    CONSTRAINT uk_tables_per_restaurant UNIQUE (restaurant_id, table_number),
    CONSTRAINT check_table_capacity CHECK (capacity > 0),
    CONSTRAINT check_table_floor CHECK (floor >= 0)
);

-- Indexes
CREATE INDEX idx_tables_restaurant_status ON tables(restaurant_id, status);
CREATE INDEX idx_tables_restaurant_number ON tables(restaurant_id, table_number);
CREATE INDEX idx_tables_current_order ON tables(current_order_id);

