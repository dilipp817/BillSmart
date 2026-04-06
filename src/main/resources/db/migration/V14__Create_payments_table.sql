-- V14: Create payments table
-- Date: 2026-03-23
-- Purpose: Payment records and transaction tracking (matches Payment entity exactly)

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,

    -- References
    bill_id BIGINT,
    order_id BIGINT NOT NULL,

    -- Payment Details
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    -- Transaction identifiers
    transaction_id VARCHAR(100),
    reference_number VARCHAR(100) NOT NULL UNIQUE,

    -- Notes
    notes TEXT,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    -- Constraints
    CONSTRAINT fk_payments_bill FOREIGN KEY (bill_id)
        REFERENCES bills(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE RESTRICT,
    CONSTRAINT check_payment_amount CHECK (amount > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_payments_bill_id ON payments(bill_id);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);
CREATE INDEX IF NOT EXISTS idx_payments_reference_number ON payments(reference_number);
