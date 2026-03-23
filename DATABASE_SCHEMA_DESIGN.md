# 🗄️ BillSmart - Complete Database Schema Design

**Version:** 1.0  
**Date:** March 23, 2026  
**Database:** PostgreSQL (Production-ready)

---

## 📊 Table of Contents

1. [Core Entities](#core-entities)
2. [Relationship Diagram](#relationship-diagram)
3. [Migration Scripts](#migration-scripts)
4. [Indexes for Performance](#indexes-for-performance)
5. [Constraints & Validation](#constraints--validation)
6. [Data Integrity Rules](#data-integrity-rules)

---

## 🏛️ Core Entities

### 1. USERS (Enhancement of existing)

**Purpose:** Authentication, authorization, device tracking

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    
    -- Authentication
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(500) NOT NULL,  -- BCrypt hashed
    
    -- Profile
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    
    -- Authorization
    role VARCHAR(50) NOT NULL DEFAULT 'staff',  -- admin, manager, staff, waiter
    restaurant_id INTEGER,  -- Which restaurant this user belongs to
    permissions TEXT,  -- JSON array of permissions
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Device tracking (for multi-device support)
    device_id VARCHAR(255),
    device_type VARCHAR(50),  -- tablet, mobile, desktop
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_users_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES restaurant(restro_id) ON DELETE SET NULL
);

-- Indexes for authentication
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_restaurant_id ON users(restaurant_id);
CREATE INDEX idx_users_is_active ON users(is_active);
```

**Fields Explanation:**
- `password`: Must be hashed using BCrypt before storage
- `role`: Use ENUM in application code for type safety
- `permissions`: JSON format for flexible permission management
- `device_id`: Unique identifier from Android device
- `device_type`: For filtering requests by device type

---

### 2. RESTAURANT (Enhancement of existing)

**Purpose:** Multi-tenant isolation, tax configuration

```sql
CREATE TABLE restaurant (
    restro_id SERIAL PRIMARY KEY,
    
    -- Basic Info
    outlet_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    outlet_manager VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Address (Embedded)
    building VARCHAR(255),
    street VARCHAR(255),
    store_location VARCHAR(255),
    zip_code VARCHAR(20),
    
    -- Tax Configuration
    tax_rate DECIMAL(5, 2) DEFAULT 18.00,  -- In percentage (18% for GST)
    gstin VARCHAR(50),  -- GST Identification Number for India
    pan_number VARCHAR(50),  -- PAN for India
    
    -- Logo/Branding
    store_logo_url VARCHAR(500),
    store_logo_media_type VARCHAR(100),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_restaurant_outlet_name ON restaurant(outlet_name);
CREATE INDEX idx_restaurant_is_active ON restaurant(is_active);
```

**Fields Explanation:**
- `tax_rate`: Store as decimal percentage (18.00 for 18%)
- `gstin`/`pan_number`: Required in India, optional in other regions
- `is_active`: Soft-delete or deactivation flag

---

### 3. CATEGORIES (New)

**Purpose:** Organize menu items into logical groups

```sql
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    
    -- Reference
    restaurant_id INTEGER NOT NULL,
    
    -- Content
    name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- Display
    display_order INTEGER NOT NULL DEFAULT 0,  -- 1, 2, 3... for ordering
    is_active BOOLEAN DEFAULT TRUE,
    image_url VARCHAR(500),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_categories_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES restaurant(restro_id) ON DELETE CASCADE,
    CONSTRAINT uk_categories_name_per_restaurant UNIQUE (restaurant_id, name)
);

-- Indexes
CREATE INDEX idx_categories_restaurant_id ON categories(restaurant_id);
CREATE INDEX idx_categories_restaurant_active ON categories(restaurant_id, is_active);
CREATE INDEX idx_categories_restaurant_order ON categories(restaurant_id, display_order);
```

**Fields Explanation:**
- `display_order`: Integer to maintain custom ordering (not alphabetical)
- `name`: Unique per restaurant (not globally unique)
- `is_active`: Soft-delete support

---

### 4. FOOD (Enhancement of existing)

**Purpose:** Menu items for restaurants

```sql
CREATE TABLE food (
    id SERIAL PRIMARY KEY,
    
    -- Reference
    restro_id INTEGER NOT NULL,
    category_id INTEGER,  -- Optional: can be null if no category
    
    -- Content
    name VARCHAR(255) NOT NULL,
    description TEXT,  -- Detailed item description
    
    -- Pricing
    price DECIMAL(10, 2) NOT NULL,  -- Always store as DECIMAL for financial data
    
    -- Media
    image_url VARCHAR(500),  -- URL to food image
    
    -- Availability
    is_available BOOLEAN DEFAULT TRUE,
    
    -- Metadata
    preparation_time INTEGER,  -- In minutes (nullable)
    allergens VARCHAR(500),  -- Comma-separated or JSON
    calories INTEGER,  -- Approximate calories (nullable)
    is_vegetarian BOOLEAN DEFAULT FALSE,
    is_spicy BOOLEAN DEFAULT FALSE,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_food_restaurant FOREIGN KEY (restro_id) 
        REFERENCES restaurant(restro_id) ON DELETE CASCADE,
    CONSTRAINT fk_food_category FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE SET NULL
);

-- Indexes for common queries
CREATE INDEX idx_food_restaurant_available ON food(restro_id, is_available);
CREATE INDEX idx_food_restaurant_category ON food(restro_id, category_id);
CREATE INDEX idx_food_name_search ON food(name);  -- For LIKE searches
CREATE INDEX idx_food_price ON food(price);  -- For price sorting
```

**Fields Explanation:**
- `price`: DECIMAL(10,2) is essential for financial accuracy
- `is_available`: Toggle without deleting record
- `allergens`: Store as structured text or JSON
- `preparation_time`: Used for kitchen management
- `is_vegetarian`/`is_spicy`: Boolean flags for filtering

---

### 5. TABLES (New)

**Purpose:** Restaurant seating management

```sql
CREATE TABLE tables (
    id SERIAL PRIMARY KEY,
    
    -- Reference
    restaurant_id INTEGER NOT NULL,
    
    -- Table Info
    table_number VARCHAR(50) NOT NULL,  -- "1", "A1", "Window-1", etc.
    floor INTEGER DEFAULT 1,  -- 0 for ground floor, 1 for first floor
    capacity INTEGER NOT NULL,  -- Number of seats
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'available',
    -- available, occupied, reserved, cleaning, maintenance
    
    -- Current Order
    current_order_id INTEGER,  -- Quick lookup for occupied tables
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_tables_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES restaurant(restro_id) ON DELETE CASCADE,
    CONSTRAINT fk_tables_order FOREIGN KEY (current_order_id) 
        REFERENCES orders(id) ON DELETE SET NULL,
    CONSTRAINT uk_tables_per_restaurant UNIQUE (restaurant_id, table_number)
);

-- Indexes
CREATE INDEX idx_tables_restaurant_status ON tables(restaurant_id, status);
CREATE INDEX idx_tables_restaurant_number ON tables(restaurant_id, table_number);
CREATE INDEX idx_tables_current_order ON tables(current_order_id);
```

**Fields Explanation:**
- `table_number`: VARCHAR to support "A1", "Window-1", etc.
- `status`: Limited set of values for state machine
- `current_order_id`: Performance optimization for status queries

---

### 6. ORDERS (New)

**Purpose:** Customer orders in the restaurant

```sql
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    
    -- Identifiers
    order_number VARCHAR(50) NOT NULL UNIQUE,  -- ORD-YYYY-MM-DD-XXX
    
    -- References
    restaurant_id INTEGER NOT NULL,
    table_id INTEGER NOT NULL,
    customer_id INTEGER,  -- NULL for walk-ins, future FK to customers table
    
    -- Order Details
    order_type VARCHAR(50) NOT NULL,  -- dine_in, takeaway, delivery
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    -- pending, confirmed, preparing, ready, completed, cancelled
    
    -- Amounts (all in DECIMAL for precision)
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12, 2) DEFAULT 0,
    tax_amount DECIMAL(12, 2) DEFAULT 0,
    total_amount DECIMAL(12, 2) DEFAULT 0,
    
    -- Notes
    notes TEXT,
    special_instructions TEXT,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_orders_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES restaurant(restro_id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_table FOREIGN KEY (table_id) 
        REFERENCES tables(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_orders_restaurant_status ON orders(restaurant_id, status);
CREATE INDEX idx_orders_table_id ON orders(table_id);
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_orders_created ON orders(created_at);
CREATE INDEX idx_orders_restaurant_created ON orders(restaurant_id, created_at);
```

**Fields Explanation:**
- `order_number`: Format enforced by application (e.g., ORD-2026-03-23-001)
- All amounts as DECIMAL(12,2) for precision
- `status`: State machine pattern (pending → confirmed → preparing → ready → completed)
- `confirmed_at`, `completed_at`: Timestamp tracking for analytics

---

### 7. ORDER_ITEMS (New)

**Purpose:** Individual items within an order

```sql
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    
    -- References
    order_id INTEGER NOT NULL,
    menu_item_id INTEGER NOT NULL,
    
    -- Quantity & Pricing
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,  -- Price at time of order
    subtotal DECIMAL(12, 2) NOT NULL,  -- quantity * unit_price
    
    -- Item Details
    variant_id INTEGER,  -- Future: for item variants (size, type, etc.)
    variant_name VARCHAR(255),  -- e.g., "Large", "Extra Spicy"
    special_instructions TEXT,  -- "No onions", "Extra sauce", etc.
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) 
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_food FOREIGN KEY (menu_item_id) 
        REFERENCES food(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);
```

**Fields Explanation:**
- `unit_price`: Stored to handle price changes over time
- `subtotal`: Pre-calculated for consistency
- `special_instructions`: Per-item customizations
- ON DELETE CASCADE: If order deleted, items deleted too

---

### 8. BILLS (New)

**Purpose:** Invoice generation from orders

```sql
CREATE TABLE bills (
    id SERIAL PRIMARY KEY,
    
    -- Identifiers
    bill_number VARCHAR(50) NOT NULL UNIQUE,  -- BILL-YYYY-MM-DD-XXX
    
    -- References
    restaurant_id INTEGER NOT NULL,
    order_id INTEGER NOT NULL UNIQUE,  -- One bill per order
    table_id INTEGER NOT NULL,
    
    -- Table Info (Denormalized for bill printing)
    table_number VARCHAR(50),
    
    -- Financial Details
    subtotal DECIMAL(12, 2) NOT NULL,
    
    -- Discount
    discount_type VARCHAR(50),  -- 'percentage' or 'fixed' (nullable for no discount)
    discount_value DECIMAL(10, 2),  -- The value (e.g., 10 for 10% or 10 for ₹10)
    discount_amount DECIMAL(12, 2) DEFAULT 0,
    
    -- Tax (India: CGST + SGST = GST)
    cgst_rate DECIMAL(5, 2) DEFAULT 9.00,  -- Central GST
    cgst_amount DECIMAL(12, 2) DEFAULT 0,
    sgst_rate DECIMAL(5, 2) DEFAULT 9.00,  -- State GST
    sgst_amount DECIMAL(12, 2) DEFAULT 0,
    total_tax DECIMAL(12, 2) DEFAULT 0,
    
    -- Total
    total_amount DECIMAL(12, 2) NOT NULL,
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'unpaid',  -- unpaid, partial, paid
    
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
        REFERENCES tables(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_bills_number ON bills(bill_number);
CREATE INDEX idx_bills_order_id ON bills(order_id);
CREATE INDEX idx_bills_restaurant_status ON bills(restaurant_id, status);
CREATE INDEX idx_bills_created ON bills(created_at);
CREATE INDEX idx_bills_restaurant_created ON bills(restaurant_id, created_at);
```

**Fields Explanation:**
- `bill_number`: Format BILL-YYYY-MM-DD-XXX
- Denormalized `table_number` for printing without joins
- CGST/SGST: Indian GST split (9% each = 18% total)
- `discount_type`: String instead of boolean for extensibility (future: schemes, coupons)
- `status`: Tracks payment status for reconciliation

---

### 9. BILL_ITEMS (New)

**Purpose:** Items detail in a bill (copy from order_items)

```sql
CREATE TABLE bill_items (
    id SERIAL PRIMARY KEY,
    
    -- References
    bill_id INTEGER NOT NULL,
    order_item_id INTEGER,  -- Reference to original order item (nullable)
    
    -- Item Details
    menu_item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    
    -- Variant Info
    variant_name VARCHAR(255),
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_bill_items_bill FOREIGN KEY (bill_id) 
        REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_items_order_item FOREIGN KEY (order_item_id) 
        REFERENCES order_items(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_bill_items_bill_id ON bill_items(bill_id);
```

**Fields Explanation:**
- Denormalized copy for audit trail and bill printing
- `order_item_id`: Link to original order item (nullable if modified)

---

### 10. PAYMENTS (New)

**Purpose:** Payment records for bills

```sql
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    
    -- Identifiers
    payment_number VARCHAR(50) NOT NULL UNIQUE,  -- TXN-YYYY-MM-DD-XXX
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    
    -- References
    bill_id INTEGER NOT NULL,
    
    -- Payment Info
    amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,  -- cash, card, upi, wallet
    
    -- Payment Details
    reference_number VARCHAR(255),  -- For card: last 4 digits, for UPI: txn id
    status VARCHAR(50) NOT NULL DEFAULT 'pending',  -- pending, completed, failed, refunded
    
    -- Idempotency
    idempotency_key VARCHAR(100),  -- For duplicate prevention
    
    -- Notes
    notes TEXT,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_payments_bill FOREIGN KEY (bill_id) 
        REFERENCES bills(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_payments_bill_id ON payments(bill_id);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_created ON payments(created_at);
CREATE UNIQUE INDEX idx_payments_idempotency ON payments(idempotency_key) 
    WHERE idempotency_key IS NOT NULL;  -- For idempotent payment processing
```

**Fields Explanation:**
- `payment_number`: Format TXN-YYYY-MM-DD-XXX
- `idempotency_key`: For preventing duplicate payment processing
- `reference_number`: Masked for security (last 4 digits of card, etc.)
- Unique index on idempotency_key for duplicate detection

---

## 🔗 Relationship Diagram

```
┌─────────────────────┐
│      Users          │
├─────────────────────┤
│ id (PK)             │
│ username            │
│ password            │
│ role                │
│ restaurant_id (FK)──┼────────┐
└─────────────────────┘        │
                               │
                        ┌──────▼─────────────┐
                        │   Restaurant       │
                        ├────────────────────┤
                        │ restro_id (PK)     │
                        │ outlet_name        │
                        │ tax_rate           │
                        └────┬───┬────┬──────┘
                             │   │    │
                 ┌───────────┘   │    └──────┐
                 │               │           │
        ┌────────▼─────┐  ┌─────▼────────┐  │
        │  Categories  │  │    Food      │  │
        ├──────────────┤  ├──────────────┤  │
        │ id (PK)      │  │ id (PK)      │  │
        │ name         │  │ name         │  │
        │ category_id  │  │ category_id──┼─►id
        └──────────────┘  │ price        │  │
                          └────┬─────────┘  │
                               │            │
                          ┌────▼────────┐   │
                          │  Tables     │   │
                          ├─────────────┤   │
                          │ id (PK)     │   │
                          │ table_no    │   │
                          │ status      │   │
                          │ order_id    │   │
                          └────┬────────┘   │
                               │            │
                          ┌────▼──────────────────┐
                          │     Orders            │
                          ├───────────────────────┤
                          │ id (PK)               │
                          │ order_number          │
                          │ table_id──────────────┼►id
                          │ restaurant_id─────────┼►restro_id
                          │ status                │
                          │ total_amount          │
                          └────┬─────────────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
            ┌───────▼────────┐     ┌─────▼─────────────┐
            │  Order_Items   │     │   Bills           │
            ├────────────────┤     ├───────────────────┤
            │ id (PK)        │     │ id (PK)           │
            │ order_id (FK)──┼────►id                  │
            │ menu_item_id   │     │ bill_number       │
            │ quantity       │     │ order_id (FK)─────┼►id
            │ unit_price     │     │ status            │
            │ subtotal       │     │ total_tax         │
            └────────────────┘     │ total_amount      │
                                   └────┬──────────────┘
                                        │
                                   ┌────▼────────────────┐
                                   │  Bill_Items        │
                                   ├────────────────────┤
                                   │ id (PK)            │
                                   │ bill_id (FK)───────┼►id
                                   │ menu_item_name     │
                                   │ quantity           │
                                   │ subtotal           │
                                   └────────────────────┘
                                        
                                   ┌────────────────────┐
                                   │  Payments          │
                                   ├────────────────────┤
                                   │ id (PK)            │
                                   │ bill_id (FK)───────┼►id
                                   │ amount             │
                                   │ payment_method     │
                                   │ status             │
                                   │ transaction_id     │
                                   └────────────────────┘
```

---

## 🔐 Constraints & Validation

### Primary Key Constraints
- All tables: `id SERIAL PRIMARY KEY` for guaranteed uniqueness
- Composite keys where needed: `(restaurant_id, table_number)` for tables

### Unique Constraints
| Table | Constraint | Reason |
|-------|-----------|--------|
| `users` | `(username)` | No duplicate accounts |
| `users` | `(email)` | Email recovery |
| `categories` | `(restaurant_id, name)` | Unique per restaurant |
| `tables` | `(restaurant_id, table_number)` | Unique per restaurant |
| `orders` | `(order_number)` | Global uniqueness |
| `bills` | `(bill_number)` | Global uniqueness |
| `bills` | `(order_id)` | One bill per order |
| `payments` | `(transaction_id)` | Global uniqueness |
| `payments` | `(idempotency_key)` | Idempotent processing |

### Foreign Key Constraints
```
ON DELETE CASCADE:
  - order_items → orders
  - bill_items → bills
  - categories → restaurant
  - food → restaurant
  - tables → restaurant

ON DELETE RESTRICT:
  - users → restaurant (prevent deleting restaurant with users)
  - orders → restaurant
  - orders → tables
  - bills → restaurant
  - bills → orders
  - bills → tables
  - payments → bills
  
ON DELETE SET NULL:
  - food → categories (keep food if category deleted)
  - tables → orders (clear current_order if order deleted)
  - users → restaurant (user continues if restaurant deleted)
```

### Check Constraints
```sql
-- In each table definition:
ALTER TABLE food ADD CONSTRAINT check_food_price CHECK (price > 0);
ALTER TABLE orders ADD CONSTRAINT check_order_total CHECK (total_amount >= 0);
ALTER TABLE bills ADD CONSTRAINT check_bill_total CHECK (total_amount >= 0);
ALTER TABLE payments ADD CONSTRAINT check_payment_amount CHECK (amount > 0);
ALTER TABLE tables ADD CONSTRAINT check_table_capacity CHECK (capacity > 0);
ALTER TABLE categories ADD CONSTRAINT check_display_order CHECK (display_order >= 0);
```

---

## 🚀 Data Integrity Rules

### Business Rules

1. **Order Creation**
   - Table must exist and be available
   - Must have at least 1 item
   - Quantities must be positive integers
   - Food items must be available (is_available = true)

2. **Bill Generation**
   - Can only be generated from completed/confirmed order
   - One bill per order (cannot regenerate)
   - Discount calculation must match business rules

3. **Payment Processing**
   - Payment amount must match bill total
   - Can only pay unpaid/partial bills
   - Duplicate payments prevented via idempotency key

4. **Table Status**
   - Status transitions: available → occupied → available
   - Cannot seat 2 orders at same table
   - Current_order_id must be NULL when status = 'available'

5. **Tax Calculation**
   ```
   subtotal = sum(item.quantity * item.unit_price)
   after_discount = subtotal - discount_amount
   cgst = after_discount * 0.09
   sgst = after_discount * 0.09
   total_tax = cgst + sgst
   total_amount = after_discount + total_tax
   ```

---

## 📈 Performance Optimization

### Must-Have Indexes
```sql
-- Authentication
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_restaurant ON users(restaurant_id);

-- Food queries (most frequent)
CREATE INDEX idx_food_restaurant_available ON food(restro_id, is_available);
CREATE INDEX idx_food_restaurant_category ON food(restro_id, category_id);
CREATE INDEX idx_food_name ON food(name);

-- Order queries
CREATE INDEX idx_orders_restaurant_status ON orders(restaurant_id, status);
CREATE INDEX idx_orders_table ON orders(table_id);
CREATE INDEX idx_orders_created ON orders(restaurant_id, created_at);

-- Bill queries
CREATE INDEX idx_bills_order ON bills(order_id);
CREATE INDEX idx_bills_restaurant_status ON bills(restaurant_id, status);

-- Payment queries
CREATE INDEX idx_payments_bill ON payments(bill_id);
CREATE INDEX idx_payments_created ON payments(created_at);

-- Table queries
CREATE INDEX idx_tables_restaurant_status ON tables(restaurant_id, status);
```

### Query Optimization Tips
- Always filter by `restaurant_id` (multi-tenant query pattern)
- Use JPA `@EntityGraph` for eager loading to prevent N+1
- Denormalize frequently-accessed data (table_number in bills)
- Use database-level pagination (LIMIT, OFFSET)

---

## ✅ Migration Order

Execute migrations in this order:
1. V5__Enhance_users_table.sql (Add columns to existing)
2. V6__Create_categories_table.sql
3. V7__Create_tables_table.sql
4. V8__Enhance_food_table.sql (Add columns to existing)
5. V9__Enhance_restaurant_table.sql (Add columns to existing)
6. V10__Create_orders_table.sql
7. V11__Create_order_items_table.sql
8. V12__Create_bills_table.sql
9. V13__Create_bill_items_table.sql
10. V14__Create_payments_table.sql
11. V15__Create_all_indexes.sql (Performance indexes)

---

**This design ensures:**
✅ Data integrity via constraints  
✅ Query performance via indexes  
✅ Multi-tenancy support  
✅ Audit trail via timestamps  
✅ Extensibility for future features

