-- V14: Create payments table
-- Date: 2026-03-23
-- Purpose: Payment records and transaction tracking

CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,

    -- References
    bill_id INTEGER NOT NULL,

    -- Payment Details
    transaction_id VARCHAR(100) UNIQUE,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',

    -- Payment Gateway
    gateway_name VARCHAR(100),
    gateway_transaction_id VARCHAR(255),
    gateway_response TEXT,

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_payments_bill FOREIGN KEY (bill_id)
        REFERENCES bills(id) ON DELETE RESTRICT,
    CONSTRAINT check_payment_amount CHECK (amount > 0)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_payments_bill_id ON payments(bill_id);
CREATE INDEX IF NOT EXISTS idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX IF NOT EXISTS idx_payments_created ON payments(created_at);

