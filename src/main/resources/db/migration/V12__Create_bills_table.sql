-- V12: Create bills table
-- Date: 2026-03-23
-- Purpose: Invoice generation from orders (matches Bill entity exactly)

CREATE TABLE IF NOT EXISTS bills (
    id BIGSERIAL PRIMARY KEY,

    -- Identifiers
    bill_number VARCHAR(50) NOT NULL UNIQUE,

    -- References
    order_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,

    -- Financial Details
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    cgst_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    sgst_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,

    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'ISSUED',

    -- Optimistic locking
    version BIGINT NOT NULL DEFAULT 0,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_bills_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES restaurant(restro_id) ON DELETE RESTRICT,
    CONSTRAINT fk_bills_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE RESTRICT,
    CONSTRAINT check_bill_subtotal CHECK (subtotal >= 0),
    CONSTRAINT check_bill_total CHECK (total_amount >= 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_bills_order_id ON bills(order_id);
CREATE INDEX IF NOT EXISTS idx_bills_restaurant_id ON bills(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_bills_bill_number ON bills(bill_number);
CREATE INDEX IF NOT EXISTS idx_bills_status ON bills(status);
CREATE INDEX IF NOT EXISTS idx_bills_created_at ON bills(created_at);
