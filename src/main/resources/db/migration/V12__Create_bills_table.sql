-- V12: Create bills table
-- Date: 2026-03-23
-- Purpose: Invoice generation from orders

CREATE TABLE IF NOT EXISTS bills (
    id SERIAL PRIMARY KEY,

    -- Identifiers
    bill_number VARCHAR(50) NOT NULL UNIQUE,

    -- References
    restaurant_id INTEGER NOT NULL,
    order_id INTEGER NOT NULL UNIQUE,
    table_id INTEGER NOT NULL,

    -- Table Info (Denormalized for printing)
    table_number VARCHAR(50),

    -- Financial Details
    subtotal DECIMAL(12, 2) NOT NULL,

    -- Discount
    discount_type VARCHAR(50),
    discount_value DECIMAL(10, 2),
    discount_amount DECIMAL(12, 2) DEFAULT 0,

    -- Tax (India: CGST + SGST = GST)
    cgst_rate DECIMAL(5, 2) DEFAULT 9.00,
    cgst_amount DECIMAL(12, 2) DEFAULT 0,
    sgst_rate DECIMAL(5, 2) DEFAULT 9.00,
    sgst_amount DECIMAL(12, 2) DEFAULT 0,
    total_tax DECIMAL(12, 2) DEFAULT 0,

    -- Total
    total_amount DECIMAL(12, 2) NOT NULL,

    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'unpaid',

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    printed_at TIMESTAMP,
    settled_at TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_bills_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES restaurant(restro_id) ON DELETE RESTRICT,
    CONSTRAINT fk_bills_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bills_table FOREIGN KEY (table_id)
        REFERENCES tables(id) ON DELETE RESTRICT,
    CONSTRAINT check_bill_subtotal CHECK (subtotal >= 0),
    CONSTRAINT check_bill_discount_value CHECK (discount_value IS NULL OR discount_value > 0),
    CONSTRAINT check_bill_discount_amount CHECK (discount_amount >= 0),
    CONSTRAINT check_bill_tax CHECK (total_tax >= 0),
    CONSTRAINT check_bill_total CHECK (total_amount >= 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_bills_number ON bills(bill_number);
CREATE INDEX IF NOT EXISTS idx_bills_order_id ON bills(order_id);
CREATE INDEX IF NOT EXISTS idx_bills_restaurant_status ON bills(restaurant_id, status);
CREATE INDEX IF NOT EXISTS idx_bills_created ON bills(created_at);
CREATE INDEX IF NOT EXISTS idx_bills_restaurant_created ON bills(restaurant_id, created_at);

