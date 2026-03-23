# ✅ BillSmart - Phase-wise Implementation Checklist

**Version:** 1.0  
**Last Updated:** March 23, 2026  
**Status:** Ready for Execution

---

## 🚀 How to Use This Checklist

1. **Print or share** this document with your team
2. **Update status** daily (Not Started → In Progress → Completed)
3. **Track blockers** and document decisions
4. **Commit mapping** to each phase to keep git history clean
5. **Test after each phase** before moving to next

---

## 📋 PHASE 0: Database Setup (1-2 Days)

**Goal:** Create solid database foundation for all subsequent phases  
**Effort:** 6-8 hours  
**Team Size:** 1 Backend Developer

### 0.1 Create V5__Enhance_users_table.sql
- [ ] Add `password` column (VARCHAR 500)
- [ ] Add `first_name`, `last_name` columns
- [ ] Add `role` column (VARCHAR 50, default 'staff')
- [ ] Add `restaurant_id` column (FK to restaurant)
- [ ] Add `permissions` column (TEXT for JSON)
- [ ] Add `is_active` column (BOOLEAN, default TRUE)
- [ ] Add `device_id`, `device_type` columns
- [ ] Add `last_login` column
- [ ] Add `updated_at` column with ON UPDATE
- [ ] Create indexes: username, email, restaurant_id, is_active
- [ ] Test migration runs without errors

**Acceptance Criteria:**
```sql
SELECT * FROM users;
-- Must show: password, first_name, last_name, role, restaurant_id, 
-- is_active, device_id, device_type, created_at, updated_at, last_login
```

### 0.2 Create V6__Create_categories_table.sql
- [ ] Create `categories` table
- [ ] Columns: id, restaurant_id, name, description, display_order, is_active, image_url, created_at, updated_at
- [ ] Primary key: id
- [ ] Foreign key: restaurant_id → restaurant (ON DELETE CASCADE)
- [ ] Unique constraint: (restaurant_id, name)
- [ ] Create indexes: restaurant_id, (restaurant_id, is_active), (restaurant_id, display_order)

**Acceptance Criteria:**
```sql
INSERT INTO categories (restaurant_id, name, display_order) 
VALUES (1, 'Main Course', 1);
SELECT * FROM categories;
```

### 0.3 Create V7__Create_tables_table.sql
- [ ] Create `tables` table
- [ ] Columns: id, restaurant_id, table_number, floor, capacity, status, current_order_id, created_at, updated_at
- [ ] Primary key: id
- [ ] Foreign keys: restaurant_id → restaurant, current_order_id → orders (ON DELETE SET NULL)
- [ ] Unique constraint: (restaurant_id, table_number)
- [ ] Create indexes: (restaurant_id, status), (restaurant_id, table_number)

**Acceptance Criteria:**
```sql
INSERT INTO tables (restaurant_id, table_number, capacity) 
VALUES (1, '1', 4);
SELECT * FROM tables WHERE status = 'available';
```

### 0.4 Create V8__Enhance_food_table.sql
- [ ] Add `category_id` column (FK to categories, nullable)
- [ ] Add `image_url` column (VARCHAR 500)
- [ ] Add `description` column (TEXT)
- [ ] Add `is_available` column (BOOLEAN, default TRUE)
- [ ] Add `preparation_time` column (INTEGER, nullable)
- [ ] Add `allergens` column (VARCHAR 500, nullable)
- [ ] Add `calories` column (INTEGER, nullable)
- [ ] Add `is_vegetarian` column (BOOLEAN, default FALSE)
- [ ] Add `is_spicy` column (BOOLEAN, default FALSE)
- [ ] Add `updated_at` column
- [ ] Create indexes: (restro_id, is_available), (restro_id, category_id), name, price
- [ ] Verify existing data not corrupted

**Acceptance Criteria:**
```sql
SELECT * FROM food;
-- Must show: name, price, image_url, category_id, description, 
-- is_available, preparation_time, allergens, calories, updated_at
```

### 0.5 Create V9__Enhance_restaurant_table.sql
- [ ] Add `is_active` column (BOOLEAN, default TRUE)
- [ ] Add `tax_rate` column (DECIMAL 5,2, default 18.00)
- [ ] Add `gstin` column (VARCHAR 50)
- [ ] Add `pan_number` column (VARCHAR 50)
- [ ] Add `updated_at` column with ON UPDATE
- [ ] Verify logo columns still exist

**Acceptance Criteria:**
```sql
SELECT tax_rate FROM restaurant WHERE restro_id = 1;
-- Must return: 18.00
```

### 0.6 Create V10__Create_orders_table.sql
- [ ] Create `orders` table
- [ ] Columns: id, order_number, restaurant_id, table_id, customer_id, order_type, status, 
       subtotal, discount_amount, tax_amount, total_amount, notes, special_instructions,
       created_at, updated_at, confirmed_at, completed_at
- [ ] Primary key: id
- [ ] Foreign keys: restaurant_id → restaurant (RESTRICT), table_id → tables (RESTRICT)
- [ ] Unique constraint: order_number
- [ ] Check constraints: total_amount >= 0
- [ ] Create indexes: (restaurant_id, status), table_id, order_number, created_at, 
       (restaurant_id, created_at)

**Acceptance Criteria:**
```sql
INSERT INTO orders (order_number, restaurant_id, table_id, order_type, status, total_amount)
VALUES ('ORD-2026-03-23-001', 1, 1, 'dine_in', 'pending', 0);
```

### 0.7 Create V11__Create_order_items_table.sql
- [ ] Create `order_items` table
- [ ] Columns: id, order_id, menu_item_id, quantity, unit_price, subtotal, 
       variant_id, variant_name, special_instructions, created_at
- [ ] Primary key: id
- [ ] Foreign keys: order_id → orders (CASCADE), menu_item_id → food (RESTRICT)
- [ ] Create indexes: order_id, menu_item_id
- [ ] Check constraints: quantity > 0, unit_price >= 0

**Acceptance Criteria:**
```sql
INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal)
VALUES (1, 1, 2, 350.00, 700.00);
```

### 0.8 Create V12__Create_bills_table.sql
- [ ] Create `bills` table
- [ ] Columns: id, bill_number, restaurant_id, order_id, table_id, table_number,
       subtotal, discount_type, discount_value, discount_amount, cgst_rate, cgst_amount,
       sgst_rate, sgst_amount, total_tax, total_amount, status, created_at, printed_at, settled_at
- [ ] Primary key: id
- [ ] Foreign keys: restaurant_id → restaurant (RESTRICT), order_id → orders (RESTRICT), 
       table_id → tables (RESTRICT)
- [ ] Unique constraints: bill_number, order_id
- [ ] Create indexes: bill_number, order_id, (restaurant_id, status), created_at, 
       (restaurant_id, created_at)

**Acceptance Criteria:**
```sql
INSERT INTO bills (bill_number, restaurant_id, order_id, table_id, table_number, 
                   subtotal, total_amount, status)
VALUES ('BILL-2026-03-23-001', 1, 1, 1, '1', 950.00, 1121.00, 'unpaid');
```

### 0.9 Create V13__Create_bill_items_table.sql
- [ ] Create `bill_items` table
- [ ] Columns: id, bill_id, order_item_id, menu_item_name, quantity, unit_price, 
       subtotal, variant_name, created_at
- [ ] Primary key: id
- [ ] Foreign keys: bill_id → bills (CASCADE), order_item_id → order_items (SET NULL)
- [ ] Create indexes: bill_id

**Acceptance Criteria:**
```sql
INSERT INTO bill_items (bill_id, menu_item_name, quantity, unit_price, subtotal)
VALUES (1, 'Butter Chicken', 2, 350.00, 700.00);
```

### 0.10 Create V14__Create_payments_table.sql
- [ ] Create `payments` table
- [ ] Columns: id, payment_number, transaction_id, bill_id, amount, payment_method,
       reference_number, status, idempotency_key, notes, created_at, completed_at
- [ ] Primary key: id
- [ ] Foreign key: bill_id → bills (RESTRICT)
- [ ] Unique constraints: transaction_id, (idempotency_key) where idempotency_key IS NOT NULL
- [ ] Create indexes: bill_id, transaction_id, created_at
- [ ] Check constraints: amount > 0

**Acceptance Criteria:**
```sql
INSERT INTO payments (payment_number, transaction_id, bill_id, amount, payment_method, status)
VALUES ('TXN-2026-03-23-001', 'TXN-2026-03-23-001', 1, 1121.00, 'cash', 'completed');
```

### 0.11 Create V15__Create_all_indexes.sql
- [ ] Verify all indexes from above migrations exist
- [ ] Add any missing performance indexes
- [ ] Test query performance with EXPLAIN ANALYZE

**Acceptance Criteria:**
```sql
-- Run these queries to verify indexes are being used
EXPLAIN ANALYZE SELECT * FROM food WHERE restro_id = 1 AND is_available = TRUE;
EXPLAIN ANALYZE SELECT * FROM orders WHERE restaurant_id = 1 AND status = 'pending';
```

### 0.12 Database Validation
- [ ] Run all migrations successfully
- [ ] Check for migration errors in logs
- [ ] Verify data integrity (no orphaned records)
- [ ] Test foreign key constraints work
- [ ] Create backup of database schema

**Commands:**
```bash
# Check migration status
SELECT * FROM flyway_schema_history;

# Verify table structure
DESCRIBE users;
DESCRIBE categories;
DESCRIBE tables;
DESCRIBE orders;
DESCRIBE order_items;
DESCRIBE bills;
DESCRIBE bill_items;
DESCRIBE payments;

# Verify indexes
SHOW INDEXES FROM food;
SHOW INDEXES FROM orders;
SHOW INDEXES FROM bills;
```

---

## 🔴 PHASE 1: Authentication & Core APIs (4-6 Days)

**Goal:** MVP with login and food listing  
**Effort:** 20-24 hours  
**Endpoints Delivered:** 3

### 1.1: Authentication API - Login

#### 1.1.1 Create Auth Entities & DTOs
- [ ] Create `User` entity (enhance existing)
  - [ ] Add @Column annotations for all new fields
  - [ ] Add @Embedded for address (future)
  - [ ] Add validation annotations
  - [ ] Add @EntityGraph for efficient loading
- [ ] Create `AuthRequest` DTO
  ```kotlin
  data class LoginRequest(
      @NotBlank val username: String,
      @NotBlank val password: String,
      val device_id: String? = null,
      val device_type: String? = null
  )
  ```
- [ ] Create `AuthResponse` DTO
  ```kotlin
  data class LoginResponse(
      val access_token: String,
      val refresh_token: String,
      val token_type: String,
      val expires_in: Long,
      val user: UserResponse
  )
  ```
- [ ] Create `TokenResponse` DTO

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
├── dto/auth/
│   ├── LoginRequest.kt
│   ├── LoginResponse.kt
│   ├── TokenResponse.kt
│   └── UserResponse.kt
```

**Commit Message:** `feat: Add authentication DTOs and enhance User entity`

#### 1.1.2 Create JWT Configuration
- [ ] Create `JwtProperties` class (read from application.properties)
- [ ] Create `TokenProvider` service for token generation/validation
- [ ] Create `JwtFilter` for request filtering
- [ ] Create `JwtConfig` configuration class
- [ ] Add JWT dependencies to build.gradle.kts (io.jsonwebtoken:jjwt)

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
├── config/
│   ├── JwtConfig.kt
│   ├── JwtProperties.kt
│   └── SecurityConfig.kt
├── filter/
│   └── JwtFilter.kt
├── util/
│   └── TokenProvider.kt
```

**Commit Message:** `feat: Add JWT configuration and security filter`

#### 1.1.3 Create Password Encoding
- [ ] Create `PasswordEncoder` utility (BCrypt)
- [ ] Add BCrypt dependency to build.gradle.kts
- [ ] Create password hashing utility methods

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── util/
    └── PasswordEncoder.kt
```

**Commit Message:** `feat: Add BCrypt password encoding`

#### 1.1.4 Create Auth Service
- [ ] Create `AuthService` interface
- [ ] Create `AuthServiceImpl` implementation
  - [ ] Login logic with password verification
  - [ ] Token generation
  - [ ] Device tracking
  - [ ] Error handling (invalid credentials, disabled account)
  - [ ] Update last_login timestamp
- [ ] Add user repository methods for finding by username

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
├── service/
│   └── auth/
│       ├── AuthService.kt
│       └── AuthServiceImpl.kt
└── repository/
    └── UserRepository.kt (enhance)
```

**Commit Message:** `feat: Implement authentication service with JWT tokens`

#### 1.1.5 Create Auth Controller
- [ ] Create `AuthController`
- [ ] Implement POST `/api/v1/auth/login` endpoint
- [ ] Implement response wrapper
- [ ] Add error handling
- [ ] Add request validation
- [ ] Return access token, refresh token, user info

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── controller/
    └── AuthController.kt
```

**Commit Message:** `feat: Add authentication REST endpoint`

#### 1.1.6 Update Configuration Files
- [ ] Add JWT configuration to `application.properties`
  ```properties
  jwt.secret=your-super-secret-key-minimum-256-bits
  jwt.expiration=3600000
  jwt.refresh.expiration=604800000
  ```
- [ ] Add JWT config to `application-dev.properties`
- [ ] Add JWT config to `application-prod.properties` (with env variables)

**Commit Message:** `chore: Add JWT configuration properties`

#### 1.1.7 Create Test Data
- [ ] Create migration to insert test user with hashed password
  ```sql
  INSERT INTO users (username, email, password, first_name, last_name, role, restaurant_id, is_active)
  VALUES ('admin@restaurant.com', 'admin@restaurant.com', '$2a$10$...hashed...', 'Admin', 'User', 'admin', 1, TRUE);
  ```

**Commit Message:** `test: Add test users for authentication testing`

#### 1.1.8 Test Authentication Endpoint
- [ ] Start application
- [ ] Test login with correct credentials (should return token)
- [ ] Test login with wrong credentials (should return 401)
- [ ] Test token expiration
- [ ] Test refresh token logic (if implemented)
- [ ] Add to Postman collection

**Test Commands:**
```bash
# Create test request
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@restaurant.com",
    "password": "password123",
    "device_id": "tablet-001",
    "device_type": "tablet"
  }'

# Expected response
{
  "success": true,
  "data": {
    "access_token": "eyJhbGci...",
    "refresh_token": "eyJhbGci...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "user": {...}
  }
}
```

**Commit Message:** `test: Add authentication endpoint tests`

---

### 1.2: Foods API - Get Foods with Pagination & Filtering

#### 1.2.1 Update Food Entity & Repository
- [ ] Verify Food entity has all required columns
- [ ] Create custom JPA queries for filtering
- [ ] Add pagination support (Spring Data Page/Pageable)
- [ ] Create specification class for dynamic queries (or custom QueryDSL)

**Repository Methods Needed:**
```kotlin
interface FoodRepository : JpaRepository<Food, Long> {
    fun findByRestroIdAndIsAvailable(restroId: Long, isAvailable: Boolean, pageable: Pageable): Page<Food>
    fun findByRestroIdAndCategoryIdAndIsAvailable(restroId: Long, categoryId: Long, isAvailable: Boolean, pageable: Pageable): Page<Food>
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Food>
    // ... more queries for filters
}
```

**Commit Message:** `feat: Add Food repository with filtering queries`

#### 1.2.2 Create Food DTOs
- [ ] Create `FoodResponse` DTO (with all fields)
- [ ] Create `PaginatedResponse<T>` wrapper
- [ ] Create `FoodFilterRequest` for query parameters

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── dto/food/
    ├── FoodResponse.kt
    ├── PaginatedResponse.kt
    └── FoodFilterRequest.kt
```

**Commit Message:** `feat: Add Food API DTOs`

#### 1.2.3 Create Food Service
- [ ] Create `FoodService` interface
- [ ] Create `FoodServiceImpl` with:
  - [ ] Get foods with pagination
  - [ ] Filter by category
  - [ ] Filter by is_available
  - [ ] Sort by price, name
  - [ ] Search by name
  - [ ] Combine multiple filters
  - [ ] Calculate pagination metadata (current_page, has_more, total)
  - [ ] Error handling

**Logic:**
```kotlin
fun getFoods(
    restaurantId: Long,
    offset: Int = 0,
    limit: Int = 20,
    category: String? = null,
    search: String? = null,
    sort: String? = null,
    isAvailable: Boolean? = null
): PaginatedResponse<FoodResponse>
```

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── service/food/
    ├── FoodService.kt
    └── FoodServiceImpl.kt
```

**Commit Message:** `feat: Implement Food service with filtering and pagination`

#### 1.2.4 Create Food Controller
- [ ] Create `FoodController` REST endpoint
- [ ] Implement GET `/api/v1/foods` endpoint
- [ ] Add query parameter validation
- [ ] Add response wrapper
- [ ] Add error handling

**Endpoint Specification:**
```http
GET /api/v1/foods?offset=0&limit=20&category=Main%20Course&sort=price:asc&search=chicken&is_available=true
```

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── controller/
    └── FoodController.kt
```

**Commit Message:** `feat: Add Foods API endpoint with filtering`

#### 1.2.5 Create Validator for Query Parameters
- [ ] Validate offset (non-negative)
- [ ] Validate limit (1-100)
- [ ] Validate sort parameter (only allowed values)
- [ ] Return proper error messages

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── validator/
    └── FoodQueryValidator.kt
```

**Commit Message:** `feat: Add query parameter validation for Foods API`

#### 1.2.6 Test Foods Endpoint
- [ ] Create test food items
- [ ] Test pagination (offset, limit)
- [ ] Test category filter
- [ ] Test search functionality
- [ ] Test sort (all 4 variations: price:asc, price:desc, name:asc, name:desc)
- [ ] Test combined filters
- [ ] Test invalid parameters
- [ ] Verify pagination metadata
- [ ] Add to Postman collection

**Test Commands:**
```bash
# Basic pagination
curl http://localhost:8080/api/v1/foods?offset=0&limit=20

# With category filter
curl "http://localhost:8080/api/v1/foods?category=Main%20Course&offset=0&limit=20"

# With sort
curl "http://localhost:8080/api/v1/foods?sort=price:asc&offset=0&limit=20"

# Combined
curl "http://localhost:8080/api/v1/foods?category=Beverages&sort=name:asc&search=coffee&offset=0&limit=10"
```

**Commit Message:** `test: Add comprehensive Foods API tests`

---

### 1.3: Categories API - Get All Categories

#### 1.3.1 Create Category Entity
- [ ] Create `Category` entity class
- [ ] Add all fields from schema
- [ ] Add validation annotations
- [ ] Add @ManyToOne relationship to Restaurant

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── model/
    └── Category.kt
```

**Commit Message:** `feat: Create Category entity`

#### 1.3.2 Create Category Repository
- [ ] Create `CategoryRepository` interface
- [ ] Implement query: findByRestaurantIdAndIsActive(restaurantId, isActive)
- [ ] Add ordering by display_order
- [ ] Implement query: count items per category

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── repository/
    └── CategoryRepository.kt
```

**Commit Message:** `feat: Add Category repository with queries`

#### 1.3.3 Create Category DTOs
- [ ] Create `CategoryResponse` DTO with item_count
- [ ] Create mapper for Category → CategoryResponse

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
├── dto/category/
│   └── CategoryResponse.kt
└── mapper/
    └── CategoryMapper.kt
```

**Commit Message:** `feat: Add Category DTOs and mapper`

#### 1.3.4 Create Category Service
- [ ] Create `CategoryService` interface
- [ ] Create `CategoryServiceImpl` with:
  - [ ] Get all categories by restaurant
  - [ ] Filter by is_active
  - [ ] Order by display_order
  - [ ] Calculate item_count per category
  - [ ] Error handling

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── service/category/
    ├── CategoryService.kt
    └── CategoryServiceImpl.kt
```

**Commit Message:** `feat: Implement Category service`

#### 1.3.5 Create Category Controller
- [ ] Create `CategoryController` REST endpoint
- [ ] Implement GET `/api/v1/categories` endpoint
- [ ] Add query parameter support (restaurant_id, is_active)
- [ ] Add response wrapper
- [ ] Add error handling

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── controller/
    └── CategoryController.kt
```

**Commit Message:** `feat: Add Categories API endpoint`

#### 1.3.6 Test Categories Endpoint
- [ ] Create test categories
- [ ] Test GET all categories
- [ ] Verify item_count calculation
- [ ] Verify display_order sorting
- [ ] Test filter by is_active
- [ ] Add to Postman collection

**Test Commands:**
```bash
curl http://localhost:8080/api/v1/categories

# With active filter
curl "http://localhost:8080/api/v1/categories?is_active=true"
```

**Commit Message:** `test: Add Categories API tests`

---

### 1.4 Error Handling & Response Wrapper

#### 1.4.1 Create Global Exception Handler
- [ ] Create custom exceptions:
  ```kotlin
  sealed class AppException : RuntimeException() {
      data class ValidationException(val details: Map<String, String>)
      data class ResourceNotFoundException(val message: String)
      data class UnauthorizedException(val message: String)
      data class BadRequestException(val message: String)
  }
  ```
- [ ] Create `GlobalExceptionHandler` with @RestControllerAdvice
- [ ] Map exceptions to HTTP status codes
- [ ] Return standardized error responses

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
├── exception/
│   ├── AppException.kt
│   └── ErrorCode.kt
└── controller/
    └── GlobalExceptionHandler.kt
```

**Commit Message:** `feat: Add global exception handling and error responses`

#### 1.4.2 Create Response Wrapper
- [ ] Create `ApiResponse<T>` wrapper class
- [ ] Create `ErrorDetails` class
- [ ] Use in all endpoints
- [ ] Ensure consistent format

**Files to Create:**
```
src/main/kotlin/com/autobill/billsmart/
└── dto/
    ├── ApiResponse.kt
    └── ErrorDetails.kt
```

**Commit Message:** `feat: Add API response wrapper`

---

### 1.5 Documentation & Postman

#### 1.5.1 Create Postman Collection
- [ ] Add 3 endpoints to collection:
  - POST /api/v1/auth/login
  - GET /api/v1/foods (with all filter examples)
  - GET /api/v1/categories
- [ ] Include example requests and responses
- [ ] Add authentication header setup
- [ ] Save as `postman_collection_phase1.json`

**Commit Message:** `docs: Add Postman collection for Phase 1 APIs`

#### 1.5.2 API Documentation
- [ ] Create `PHASE1_API_DOCS.md` with:
  - Endpoint specifications
  - Request/response examples
  - Error codes
  - Testing instructions

**Commit Message:** `docs: Add Phase 1 API documentation`

---

### 1.6 Phase 1 Completion Checklist

- [ ] All 3 endpoints working (login, get foods, get categories)
- [ ] Pagination verified with test data
- [ ] Filtering working correctly
- [ ] Sorting working for all 4 variations
- [ ] Error handling working
- [ ] Response format matches specification
- [ ] All 15 migration files applied successfully
- [ ] Authentication required for protected endpoints
- [ ] Postman collection created and tested
- [ ] Code reviewed and approved
- [ ] All tests passing
- [ ] Documentation complete

**Phase 1 Summary:**
```
✅ 3 API Endpoints
✅ 11 Source files created
✅ 15 Migration files
✅ Full Postman collection
✅ Comprehensive error handling
✅ Authentication working
```

**Estimated Time:** 4-6 days  
**Estimated Story Points:** 20-25

---

## 🟡 PHASE 2: Orders System (5-6 Days)

**Goal:** Full order creation, retrieval, and management  
**Effort:** 24-28 hours  
**Endpoints Delivered:** 3

### 2.1 Create Order Entities
- [ ] Create `Order` entity
- [ ] Create `OrderItem` entity
- [ ] Create relationships between Order, OrderItem, Food, Table
- [ ] Add all validation annotations
- [ ] Add audit fields (created_at, updated_at, confirmed_at, completed_at)

### 2.2 Create Order DTOs
- [ ] Create `CreateOrderRequest` DTO
- [ ] Create `OrderResponse` DTO
- [ ] Create `OrderItemResponse` DTO
- [ ] Create `UpdateOrderRequest` DTO

### 2.3 Create Order Service
- [ ] Implement order creation logic
- [ ] Validate table exists and is available
- [ ] Calculate subtotal, tax, total
- [ ] Generate order_number (ORD-YYYY-MM-DD-XXX)
- [ ] Update table status to 'occupied'
- [ ] Handle atomic transactions
- [ ] Implement order retrieval
- [ ] Implement order updates
- [ ] Add to/remove items from order
- [ ] Prevent invalid status transitions

### 2.4 Create Order Controller
- [ ] POST /api/v1/orders - Create order
- [ ] GET /api/v1/orders/{id} - Get order
- [ ] PATCH /api/v1/orders/{id} - Update order
- [ ] Add authentication/authorization
- [ ] Add request validation

### 2.5 Tax Calculation Utility
- [ ] Create `TaxCalculator` utility
- [ ] Implement GST calculation (18% total = 9% CGST + 9% SGST)
- [ ] Handle percentage and fixed discounts
- [ ] Ensure decimal precision

### 2.6 Order Number Generation
- [ ] Create `NumberGenerator` utility
- [ ] Generate order_number: ORD-YYYY-MM-DD-XXX
- [ ] Handle concurrency and uniqueness
- [ ] Implement sequence counter per day

### 2.7 Test Order APIs
- [ ] Create order with valid data
- [ ] Verify calculations
- [ ] Test order status transitions
- [ ] Test table status updates
- [ ] Test adding/removing items
- [ ] Test error cases (invalid table, empty items)
- [ ] Add to Postman collection

### 2.8 Phase 2 Deliverables
- [ ] 3 new API endpoints
- [ ] 7 new source files (entities, services, controller)
- [ ] 2 migration files (orders, order_items)
- [ ] Updated Postman collection
- [ ] Comprehensive tests

**Phase 2 Summary:**
```
✅ 3 Order API Endpoints
✅ 7 Source files
✅ 2 Migration files
✅ Complex business logic (calculations, state machine)
✅ Full CRUD operations
```

**Estimated Time:** 5-6 days  
**Estimated Story Points:** 30-35

---

## 🟡 PHASE 3: Billing System (4-5 Days)

**Goal:** Generate bills, calculate taxes, manage discounts  
**Effort:** 16-20 hours  
**Endpoints Delivered:** 2

### 3.1 Create Bill Entities
- [ ] Create `Bill` entity
- [ ] Create `BillItem` entity
- [ ] Create relationships

### 3.2 Create Bill DTOs
- [ ] Create `GenerateBillRequest` DTO
- [ ] Create `BillResponse` DTO
- [ ] Create `BillItemResponse` DTO
- [ ] Create `DiscountDetails` DTO
- [ ] Create `TaxDetails` DTO

### 3.3 Bill Generation Service
- [ ] Implement bill creation from order
- [ ] Copy order items to bill items
- [ ] Calculate discount (percentage or fixed)
- [ ] Calculate CGST + SGST
- [ ] Handle decimal precision
- [ ] Generate bill_number (BILL-YYYY-MM-DD-XXX)
- [ ] Prevent duplicate bills
- [ ] Set bill status to 'unpaid'

### 3.4 Bill Controller
- [ ] POST /api/v1/bills - Generate bill
- [ ] GET /api/v1/bills/{id} - Get bill
- [ ] Add authentication/authorization
- [ ] Add request validation

### 3.5 Discount Calculation
- [ ] Support percentage discounts
- [ ] Support fixed amount discounts
- [ ] Apply discount before tax
- [ ] Validate discount doesn't exceed subtotal

### 3.6 Test Bill APIs
- [ ] Generate bill from order
- [ ] Verify bill_number generation
- [ ] Test discount calculations
- [ ] Test tax calculations (CGST + SGST)
- [ ] Verify total_amount
- [ ] Test error cases
- [ ] Add to Postman collection

### 3.7 Phase 3 Deliverables
- [ ] 2 new API endpoints
- [ ] 7 new source files
- [ ] 2 migration files
- [ ] Updated Postman collection

**Estimated Time:** 4-5 days  
**Estimated Story Points:** 20-25

---

## 🟢 PHASE 4: Payments & Tables (4-5 Days)

**Goal:** Payment processing and table management  
**Effort:** 12-16 hours  
**Endpoints Delivered:** 2

### 4.1 Create Payment Entity
- [ ] Create `Payment` entity
- [ ] Add all required fields
- [ ] Add audit fields

### 4.2 Create Table Entity
- [ ] Create `Table` entity
- [ ] Add all required fields
- [ ] Verify relationships

### 4.3 Payment Service
- [ ] Implement payment processing
- [ ] Support multiple payment methods (cash, card, upi, wallet)
- [ ] Generate transaction_id (TXN-YYYY-MM-DD-XXX)
- [ ] Implement idempotency (X-Idempotency-Key)
- [ ] Update bill status (unpaid → paid)
- [ ] Update table status (occupied → available)
- [ ] Handle partial payments

### 4.4 Controllers
- [ ] Payment Controller: POST /api/v1/payments
- [ ] Table Controller: GET /api/v1/tables

### 4.5 Idempotency Service
- [ ] Track idempotency keys
- [ ] Prevent duplicate payments
- [ ] Return cached response for duplicate requests

### 4.6 Test Payments & Tables
- [ ] Process various payment methods
- [ ] Verify idempotency works
- [ ] Test error cases
- [ ] Verify bill and table status updates
- [ ] Add to Postman collection

### 4.7 Phase 4 Deliverables
- [ ] 2 new API endpoints
- [ ] 11 new source files
- [ ] 2 migration files
- [ ] Updated Postman collection

**Estimated Time:** 4-5 days  
**Estimated Story Points:** 18-22

---

## 🟢 PHASE 5: Additional Endpoints (2-3 Days)

**Goal:** Search and detail endpoints  
**Effort:** 8-10 hours  
**Endpoints Delivered:** 2

### 5.1 Search Foods API
- [ ] Implement full-text search
- [ ] Search in name and description
- [ ] Combine with pagination
- [ ] GET /api/v1/foods/search?q=chicken

### 5.2 Get Food by ID
- [ ] Retrieve single food item with details
- [ ] GET /api/v1/foods/{id}
- [ ] Include extended fields (preparation_time, allergens, calories, ingredients)

### 5.3 Final Testing
- [ ] Complete integration tests
- [ ] End-to-end flow: Create order → Generate bill → Process payment
- [ ] Performance testing
- [ ] Load testing with pagination

### 5.4 Phase 5 Deliverables
- [ ] 2 new API endpoints
- [ ] Final Postman collection (12 total endpoints)
- [ ] Performance optimization documentation

**Estimated Time:** 2-3 days  
**Estimated Story Points:** 10-12

---

## 📊 Overall Implementation Summary

| Phase | Duration | Endpoints | Story Points | Status |
|-------|----------|-----------|--------------|--------|
| 0: Database | 1-2 days | 0 | 8 | ⏳ |
| 1: Auth & Foods | 4-6 days | 3 | 25 | ⏳ |
| 2: Orders | 5-6 days | 3 | 35 | ⏳ |
| 3: Billing | 4-5 days | 2 | 22 | ⏳ |
| 4: Payments | 4-5 days | 2 | 20 | ⏳ |
| 5: Search | 2-3 days | 2 | 12 | ⏳ |
| **TOTAL** | **3-4 weeks** | **12** | **122** | ⏳ |

---

## ✅ Daily Standup Template

```
Date: _______________
Team Member: _______________
Phase: _______________

✅ Completed:
- [ ] Task 1
- [ ] Task 2

🔄 In Progress:
- [ ] Task 3
- [ ] Task 4

🚧 Blockers:
- Issue: ________________
  Impact: ________________
  Resolution: ____________

📝 Notes:
- ________________
```

---

## 🎯 Success Criteria for Each Phase

### Phase 0 ✅
- All 15 migrations run successfully
- No data migration errors
- Foreign keys and constraints verified
- Indexes created and tested

### Phase 1 ✅
- 3 endpoints working
- Authentication required
- Pagination working correctly
- Response format matches spec

### Phase 2 ✅
- Orders can be created/retrieved/updated
- Calculations verified
- Table status updates
- State machine working

### Phase 3 ✅
- Bills generated correctly
- Tax calculations accurate
- Discount applied correctly
- Bill status tracking

### Phase 4 ✅
- Payments processed
- Idempotency working
- Bill status updated
- Table status cleared

### Phase 5 ✅
- Search functionality working
- Detail endpoints returning extended info
- All endpoints documented
- Performance acceptable

---

**Ready to start? 🚀 Begin with Phase 0!**

