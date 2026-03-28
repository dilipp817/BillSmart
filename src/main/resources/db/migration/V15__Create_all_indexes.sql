-- V15: Create remaining indexes and performance optimization
-- Date: 2026-03-23
-- Purpose: All additional indexes for query optimization

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_restaurant_id ON users(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

-- Restaurant indexes
CREATE INDEX IF NOT EXISTS idx_restaurant_outlet_name ON restaurant(outlet_name);
CREATE INDEX IF NOT EXISTS idx_restaurant_is_active ON restaurant(is_active);

-- Categories indexes
CREATE INDEX IF NOT EXISTS idx_categories_restaurant_id ON categories(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_categories_restaurant_active ON categories(restaurant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_categories_restaurant_order ON categories(restaurant_id, display_order);

-- Food indexes
CREATE INDEX IF NOT EXISTS idx_food_restaurant_available ON food(restro_id, is_available);
CREATE INDEX IF NOT EXISTS idx_food_restaurant_category ON food(restro_id, category_id);
CREATE INDEX IF NOT EXISTS idx_food_name_search ON food(name);
CREATE INDEX IF NOT EXISTS idx_food_price ON food(price);
CREATE INDEX IF NOT EXISTS idx_food_is_vegetarian ON food(is_vegetarian);
CREATE INDEX IF NOT EXISTS idx_food_is_spicy ON food(is_spicy);

-- Tables indexes
CREATE INDEX IF NOT EXISTS idx_tables_restaurant_status ON tables(restaurant_id, status);
CREATE INDEX IF NOT EXISTS idx_tables_restaurant_number ON tables(restaurant_id, table_number);
CREATE INDEX IF NOT EXISTS idx_tables_current_order ON tables(current_order_id);

-- Orders indexes
CREATE INDEX IF NOT EXISTS idx_orders_restaurant_status ON orders(restaurant_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_table_id ON orders(table_id);
CREATE INDEX IF NOT EXISTS idx_orders_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_orders_created ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_restaurant_created ON orders(restaurant_id, created_at);

-- Order items indexes
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_menu_item_id ON order_items(menu_item_id);

-- Bills indexes
CREATE INDEX IF NOT EXISTS idx_bills_number ON bills(bill_number);
CREATE INDEX IF NOT EXISTS idx_bills_order_id ON bills(order_id);
CREATE INDEX IF NOT EXISTS idx_bills_restaurant_status ON bills(restaurant_id, status);
CREATE INDEX IF NOT EXISTS idx_bills_created ON bills(created_at);
CREATE INDEX IF NOT EXISTS idx_bills_restaurant_created ON bills(restaurant_id, created_at);

-- Bill items indexes
CREATE INDEX IF NOT EXISTS idx_bill_items_bill_id ON bill_items(bill_id);
CREATE INDEX IF NOT EXISTS idx_bill_items_order_item_id ON bill_items(order_item_id);

