-- V3__Create_food_table.sql
-- Create the food table with foreign key to restaurant

CREATE TABLE IF NOT EXISTS food (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION,
    restro_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_food_restaurant FOREIGN KEY (restro_id) REFERENCES restaurant(restro_id) ON DELETE CASCADE
);

CREATE INDEX idx_food_name ON food(name);
CREATE INDEX idx_food_restro_id ON food(restro_id);

