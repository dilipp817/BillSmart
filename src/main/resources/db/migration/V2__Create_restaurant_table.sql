-- V2__Create_restaurant_table.sql
-- Create the restaurant table with embedded address and logo

CREATE TABLE IF NOT EXISTS restaurant (
    restro_id SERIAL PRIMARY KEY,
    outlet_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    outlet_manager VARCHAR(255) NOT NULL,
    building VARCHAR(255),
    street VARCHAR(255),
    store_location VARCHAR(255),
    zip_code VARCHAR(20),
    store_logo_url VARCHAR(500),
    store_logo_media_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_restaurant_outlet_name ON restaurant(outlet_name);

