-- V4__Create_staff_table.sql
-- Create the staff table with embedded address

CREATE TABLE IF NOT EXISTS staff (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(50) NOT NULL,
    age VARCHAR(10) NOT NULL,
    salary VARCHAR(20) NOT NULL,
    building VARCHAR(255),
    street VARCHAR(255),
    store_location VARCHAR(255),
    zip_code VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_staff_name ON staff(name);

