-- V17: Seed test data for staging/testing environment
-- Date: 2026-04-04
-- Purpose: Pre-populate DB so mobile team can test all APIs immediately
--
-- TEST CREDENTIALS:
--   Admin  → username: admin    password: admin123   role: admin
--   Staff  → username: staff1   password: staff123   role: staff
--   Staff  → username: staff2   password: staff123   role: staff
--
-- RESTAURANT IDs: 1 (Spice Garden), 2 (The Burger House)
-- All passwords are BCrypt hashed (strength 10)

-- ============================================================
-- 1. RESTAURANTS
-- ============================================================

INSERT INTO restaurant (outlet_name, displayname, outlet_manager, building, street, store_location, zip_code, is_active, tax_rate, gstin)
VALUES
    ('spice_garden', 'Spice Garden', 'Rahul Sharma', 'Ground Floor, Tower A', 'MG Road', 'Bangalore', '560001', TRUE, 18.00, '29ABCDE1234F1Z5'),
    ('burger_house', 'The Burger House', 'Priya Mehta', 'Shop 12, Mall Road', 'Linking Road', 'Mumbai', '400050', TRUE, 18.00, '27XYZPQ9876G2A1');

-- ============================================================
-- 2. USERS
-- ============================================================
-- BCrypt hash of 'admin123'
-- BCrypt hash of 'staff123'

INSERT INTO users (username, email, password, role, is_active, restaurant_id)
VALUES
    ('admin',  'admin@billsmart.com',  '$2b$10$vQLVK3y8Ol7qWxFJivuhu.2MA/Vh20awGGj0lOrBdMkOhf2ptuhxm', 'admin',  TRUE, 1),
    ('staff1', 'staff1@billsmart.com', '$2b$10$XLZeQ97PVKfeYRecs6Fl3ORhcbRAHxMB8c9IP1beSYVjUEAnJP8ay', 'staff',  TRUE, 1),
    ('staff2', 'staff2@billsmart.com', '$2b$10$XLZeQ97PVKfeYRecs6Fl3ORhcbRAHxMB8c9IP1beSYVjUEAnJP8ay', 'staff',  TRUE, 2);

-- ============================================================
-- 3. CATEGORIES — Spice Garden (restaurant_id = 1)
-- ============================================================

INSERT INTO categories (restaurant_id, name, description, display_order, is_active, image_url)
VALUES
    (1, 'Starters',    'Appetizers and snacks',          1, TRUE, 'https://placehold.co/400x300?text=Starters'),
    (1, 'Main Course', 'Rice, curries and gravies',      2, TRUE, 'https://placehold.co/400x300?text=Main+Course'),
    (1, 'Breads',      'Naan, roti, paratha',            3, TRUE, 'https://placehold.co/400x300?text=Breads'),
    (1, 'Beverages',   'Lassi, juices and soft drinks',  4, TRUE, 'https://placehold.co/400x300?text=Beverages');

-- ============================================================
-- 4. CATEGORIES — The Burger House (restaurant_id = 2)
-- ============================================================

INSERT INTO categories (restaurant_id, name, description, display_order, is_active, image_url)
VALUES
    (2, 'Burgers',     'Classic and gourmet burgers',    1, TRUE, 'https://placehold.co/400x300?text=Burgers'),
    (2, 'Sides',       'Fries, rings and dips',          2, TRUE, 'https://placehold.co/400x300?text=Sides'),
    (2, 'Milkshakes',  'Thick creamy milkshakes',        3, TRUE, 'https://placehold.co/400x300?text=Milkshakes'),
    (2, 'Combos',      'Value meal combos',              4, TRUE, 'https://placehold.co/400x300?text=Combos');

-- ============================================================
-- 5. FOOD ITEMS — Spice Garden (restro_id = 1)
-- category IDs: Starters=1, Main Course=2, Breads=3, Beverages=4
-- ============================================================

INSERT INTO food (name, price, restro_id, category_id, description, image_url, is_available, is_vegetarian, is_spicy, preparation_time, calories)
VALUES
    -- Starters
    ('Paneer Tikka',         220.00, 1, 1, 'Grilled cottage cheese with spices',         'https://placehold.co/400x300?text=Paneer+Tikka',    TRUE, TRUE,  TRUE,  15, 320),
    ('Chicken Tikka',        280.00, 1, 1, 'Tender chicken marinated in spices',          'https://placehold.co/400x300?text=Chicken+Tikka',   TRUE, FALSE, TRUE,  20, 450),
    ('Veg Spring Rolls',     160.00, 1, 1, 'Crispy rolls with vegetable filling',         'https://placehold.co/400x300?text=Spring+Rolls',    TRUE, TRUE,  FALSE, 12, 280),
    ('Samosa (2 pcs)',        80.00, 1, 1, 'Classic potato-filled pastry',                'https://placehold.co/400x300?text=Samosa',          TRUE, TRUE,  FALSE, 10, 210),

    -- Main Course
    ('Dal Makhani',          320.00, 1, 2, 'Creamy black lentils slow cooked overnight',  'https://placehold.co/400x300?text=Dal+Makhani',     TRUE, TRUE,  FALSE, 25, 420),
    ('Butter Chicken',       380.00, 1, 2, 'Tender chicken in rich tomato cream sauce',   'https://placehold.co/400x300?text=Butter+Chicken',  TRUE, FALSE, FALSE, 25, 560),
    ('Palak Paneer',         300.00, 1, 2, 'Cottage cheese in spiced spinach gravy',      'https://placehold.co/400x300?text=Palak+Paneer',    TRUE, TRUE,  FALSE, 20, 390),
    ('Chicken Biryani',      420.00, 1, 2, 'Fragrant basmati rice with chicken',          'https://placehold.co/400x300?text=Biryani',         TRUE, FALSE, TRUE,  35, 650),
    ('Veg Biryani',          320.00, 1, 2, 'Fragrant basmati rice with vegetables',       'https://placehold.co/400x300?text=Veg+Biryani',     TRUE, TRUE,  FALSE, 30, 520),

    -- Breads
    ('Butter Naan',           60.00, 1, 3, 'Soft leavened bread with butter',             'https://placehold.co/400x300?text=Naan',            TRUE, TRUE,  FALSE,  8, 180),
    ('Garlic Naan',           80.00, 1, 3, 'Naan topped with garlic and butter',          'https://placehold.co/400x300?text=Garlic+Naan',     TRUE, TRUE,  FALSE,  8, 195),
    ('Tandoori Roti',         40.00, 1, 3, 'Whole wheat bread from clay oven',            'https://placehold.co/400x300?text=Roti',            TRUE, TRUE,  FALSE,  6, 120),

    -- Beverages
    ('Sweet Lassi',          120.00, 1, 4, 'Chilled sweetened yogurt drink',              'https://placehold.co/400x300?text=Sweet+Lassi',     TRUE, TRUE,  FALSE,  5, 220),
    ('Mango Lassi',          140.00, 1, 4, 'Yogurt blended with Alphonso mango',          'https://placehold.co/400x300?text=Mango+Lassi',     TRUE, TRUE,  FALSE,  5, 260),
    ('Masala Chai',           60.00, 1, 4, 'Spiced Indian tea with milk',                 'https://placehold.co/400x300?text=Masala+Chai',     TRUE, TRUE,  FALSE,  5, 90),
    ('Fresh Lime Soda',       80.00, 1, 4, 'Chilled lime soda sweet or salted',           'https://placehold.co/400x300?text=Lime+Soda',       TRUE, TRUE,  FALSE,  3, 60);

-- ============================================================
-- 6. FOOD ITEMS — The Burger House (restro_id = 2)
-- category IDs: Burgers=5, Sides=6, Milkshakes=7, Combos=8
-- ============================================================

INSERT INTO food (name, price, restro_id, category_id, description, image_url, is_available, is_vegetarian, is_spicy, preparation_time, calories)
VALUES
    -- Burgers
    ('Classic Veg Burger',   199.00, 2, 5, 'Crispy veg patty with fresh veggies',         'https://placehold.co/400x300?text=Veg+Burger',      TRUE, TRUE,  FALSE, 10, 420),
    ('Chicken Zinger',       279.00, 2, 5, 'Spicy crispy chicken fillet burger',           'https://placehold.co/400x300?text=Zinger',          TRUE, FALSE, TRUE,  12, 580),
    ('Double Smash Burger',  349.00, 2, 5, 'Double smashed beef patty with cheese',        'https://placehold.co/400x300?text=Smash+Burger',    TRUE, FALSE, FALSE, 15, 720),

    -- Sides
    ('Loaded Fries',         149.00, 2, 6, 'Crispy fries topped with cheese and jalapenos','https://placehold.co/400x300?text=Loaded+Fries',   TRUE, TRUE,  TRUE,   8, 390),
    ('Onion Rings',          129.00, 2, 6, 'Golden battered onion rings',                  'https://placehold.co/400x300?text=Onion+Rings',     TRUE, TRUE,  FALSE,  8, 310),

    -- Milkshakes
    ('Chocolate Shake',      179.00, 2, 7, 'Thick Belgian chocolate milkshake',            'https://placehold.co/400x300?text=Choco+Shake',     TRUE, TRUE,  FALSE,  5, 480),
    ('Strawberry Shake',     179.00, 2, 7, 'Fresh strawberry blended with ice cream',      'https://placehold.co/400x300?text=Strawberry+Shake',TRUE, TRUE,  FALSE,  5, 440),

    -- Combos
    ('Veg Combo',            349.00, 2, 8, 'Classic Veg Burger + Fries + Soft Drink',      'https://placehold.co/400x300?text=Veg+Combo',       TRUE, TRUE,  FALSE, 12, 780),
    ('Chicken Combo',        449.00, 2, 8, 'Chicken Zinger + Loaded Fries + Milkshake',    'https://placehold.co/400x300?text=Chicken+Combo',   TRUE, FALSE, TRUE,  15, 1150);

-- ============================================================
-- 7. TABLES — Spice Garden (restaurant_id = 1)
-- ============================================================

INSERT INTO tables (restaurant_id, table_number, capacity, floor, status)
VALUES
    (1, 'T-01', 2, 1, 'AVAILABLE'),
    (1, 'T-02', 2, 1, 'AVAILABLE'),
    (1, 'T-03', 4, 1, 'AVAILABLE'),
    (1, 'T-04', 4, 1, 'AVAILABLE'),
    (1, 'T-05', 4, 1, 'AVAILABLE'),
    (1, 'T-06', 6, 1, 'AVAILABLE'),
    (1, 'T-07', 6, 2, 'AVAILABLE'),
    (1, 'T-08', 8, 2, 'AVAILABLE');

-- ============================================================
-- 8. TABLES — The Burger House (restaurant_id = 2)
-- ============================================================

INSERT INTO tables (restaurant_id, table_number, capacity, floor, status)
VALUES
    (2, 'B-01', 2, 1, 'AVAILABLE'),
    (2, 'B-02', 2, 1, 'AVAILABLE'),
    (2, 'B-03', 4, 1, 'AVAILABLE'),
    (2, 'B-04', 4, 1, 'AVAILABLE'),
    (2, 'B-05', 6, 1, 'AVAILABLE'),
    (2, 'B-06', 6, 1, 'AVAILABLE');

