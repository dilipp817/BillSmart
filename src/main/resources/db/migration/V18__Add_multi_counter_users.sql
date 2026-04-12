-- V18: Multi-outlet / multi-counter user accounts
-- Date: 2026-04-11
--
-- Architecture:
--   One restaurant CHAIN owns multiple OUTLETs.
--   Each outlet is one row in the `restaurant` table (restaurant_id).
--   Each BILLING COUNTER inside an outlet is a dedicated user account
--   whose restaurant_id points to the outlet it belongs to.
--
-- This approach (Option 1 from MULTI_OUTLET_COUNTER_DESIGN.md) requires
-- ZERO schema changes — the restaurant_id column already exists on `users`.
--
-- Hierarchy diagram:
--
--   [Spice Garden chain]
--       └─ Spice Garden Bangalore  (restaurant_id = 1)
--              ├─ spicegarden_admin      role: admin
--              ├─ spicegarden_manager    role: manager
--              ├─ spicegarden_counter1   role: staff   ← Ground Floor tablet
--              ├─ spicegarden_counter2   role: staff   ← First Floor tablet
--              └─ spicegarden_kitchen    role: staff   ← Kitchen display (view)
--
--   [Burger House chain]
--       └─ The Burger House Mumbai   (restaurant_id = 2)
--              ├─ burgerhouse_admin      role: admin
--              ├─ burgerhouse_manager    role: manager
--              ├─ burgerhouse_counter1   role: staff
--              └─ burgerhouse_counter2   role: staff
--
-- Passwords:
--   *_admin    → Admin@123     BCrypt hash below
--   *_manager  → Manager@123  BCrypt hash below
--   *_counter* → Counter@123  BCrypt hash below
--   *_kitchen  → Kitchen@123  BCrypt hash below
--
-- All hashes generated with BCrypt strength 10.

-- ─────────────────────────────────────────────────────────────────────────────
-- SPICE GARDEN BANGALORE  (restaurant_id = 1)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO users (username, email, password, role, is_active, restaurant_id)
VALUES
-- outlet admin  (Admin@123)
('spicegarden_admin',
 'admin@spicegarden.com',
 '$2b$10$vQLVK3y8Ol7qWxFJivuhu.2MA/Vh20awGGj0lOrBdMkOhf2ptuhxm',
 'admin', TRUE, 1),

-- outlet manager  (Manager@123)
('spicegarden_manager',
 'manager@spicegarden.com',
 '$2b$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWEp5PvO',
 'manager', TRUE, 1),

-- Ground Floor counter tablet  (Counter@123)
('spicegarden_counter1',
 'counter1@spicegarden.com',
 '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p396SHiJIEe2XBIBCrQAIi',
 'staff', TRUE, 1),

-- First Floor counter tablet  (Counter@123)
('spicegarden_counter2',
 'counter2@spicegarden.com',
 '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p396SHiJIEe2XBIBCrQAIi',
 'staff', TRUE, 1),

-- Kitchen display device  (Kitchen@123)
('spicegarden_kitchen',
 'kitchen@spicegarden.com',
 '$2b$10$TwY5F6B9FsAJM3R5vPsPvO3cGMFoRnb3NgNz4tT6QaK1UKrO7SV.y',
 'staff', TRUE, 1);

-- ─────────────────────────────────────────────────────────────────────────────
-- THE BURGER HOUSE MUMBAI  (restaurant_id = 2)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO users (username, email, password, role, is_active, restaurant_id)
VALUES
-- outlet admin  (Admin@123)
('burgerhouse_admin',
 'admin@burgerhouse.com',
 '$2b$10$vQLVK3y8Ol7qWxFJivuhu.2MA/Vh20awGGj0lOrBdMkOhf2ptuhxm',
 'admin', TRUE, 2),

-- outlet manager  (Manager@123)
('burgerhouse_manager',
 'manager@burgerhouse.com',
 '$2b$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWEp5PvO',
 'manager', TRUE, 2),

-- Counter 1 tablet  (Counter@123)
('burgerhouse_counter1',
 'counter1@burgerhouse.com',
 '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p396SHiJIEe2XBIBCrQAIi',
 'staff', TRUE, 2),

-- Counter 2 tablet  (Counter@123)
('burgerhouse_counter2',
 'counter2@burgerhouse.com',
 '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p396SHiJIEe2XBIBCrQAIi',
 'staff', TRUE, 2);

