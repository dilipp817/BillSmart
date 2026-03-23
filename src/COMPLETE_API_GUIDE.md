# 🔧 SmartPos API - Complete Backend Implementation Guide

**Version:** 1.1  
**Date:** March 23, 2026  
**Status:** 🔴 **READY FOR BACKEND IMPLEMENTATION**  
**Priority:** HIGH - Android app is waiting

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Base Configuration](#base-configuration)# 🔧 SmartPos API - Complete Backend Implementation Guide

**Version:** 1.1  
**Date:** March 23, 2026  
**Status:** 🔴 **READY FOR BACKEND IMPLEMENTATION**  
**Priority:** HIGH - Android app is waiting

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Base Configuration](#base-configuration)
3. [Authentication](#authentication)
4. [Foods API](#foods-api)
5. [Categories API](#categories-api)
6. [Orders API](#orders-api)
7. [Bills API](#bills-api)
8. [Payments API](#payments-api)
9. [Tables API](#tables-api)
10. [Data Models](#data-models)
11. [Error Handling](#error-handling)
12. [Testing Checklist](#testing-checklist)

---

## 🎯 Overview

### **What This Document Contains**

Complete specifications for **ALL** APIs needed for SmartPos Android app, including:
- Exact request/response formats
- All query parameters
- Data models/schemas
- Error responses
- Authentication
- Examples for every endpoint

### **Backend Implementation Scope**

**Total Endpoints:** 8 (for MVP - Foods, Categories, Orders, Bills, Payments, Tables)

**Priority Breakdown:**
- 🔴 **CRITICAL** (3 endpoints) - Must implement first
- 🟡 **HIGH** (3 endpoints) - Implement next
- 🟢 **MEDIUM** (2 endpoints) - Can be implemented later

---

## 🌐 Base Configuration

### **Base URLs**

```
Development:  http://localhost:8080/api/v1
Staging:      https://staging-api.smartpos.com/api/v1
Production:   https://api.smartpos.com/api/v1
```

### **Standard Headers**

**All Requests Must Include:**
```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer {access_token}
X-Restaurant-ID: {restaurant_id}
```

**Optional Headers:**
```http
X-Request-ID: {uuid}              # For request tracing
X-Idempotency-Key: {uuid}         # For critical operations (payments)
```

### **Response Format (Standard Wrapper)**

**Success Response:**
```json
{
  "success": true,
  "data": { ... },
  "message": "Optional success message"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": { ... }
  }
}
```

---

## 🔐 Authentication

### **1. Login**

**Priority:** 🔴 CRITICAL

**Endpoint:**
```http
POST /api/v1/auth/login
```

**Request Body:**
```json
{
  "username": "admin@restaurant.com",
  "password": "SecurePassword123!",
  "device_id": "tablet-001",
  "device_type": "tablet"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGci...",
    "refresh_token": "eyJhbGci...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "user": {
      "id": 1,
      "username": "admin@restaurant.com",
      "first_name": "Admin",
      "last_name": "User",
      "role": "admin",
      "restaurant_id": 1,
      "permissions": ["order.*", "bill.*", "payment.*", "menu.*"]
    }
  }
}
```

**Error Responses:**
```json
// 401 Unauthorized
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid username or password"
  }
}

// 403 Forbidden
{
  "success": false,
  "error": {
    "code": "ACCOUNT_DISABLED",
    "message": "Account has been disabled"
  }
}
```

---

## 🍽️ Foods API

### **1. Get Foods (Paginated with Filters)**

**Priority:** 🔴 CRITICAL - **IMPLEMENT THIS FIRST!**

**Endpoint:**
```http
GET /api/v1/foods
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `offset` | integer | No | 0 | Starting position (0-based) |
| `limit` | integer | No | 20 | Items per page (max: 100) |
| `category` | string | No | null | Filter by exact category name |
| `sort` | string | No | null | Sort order (see below) |
| `search` | string | No | null | Search in item name |
| `restaurant_id` | integer | No | From header | Restaurant ID |
| `is_available` | boolean | No | null | Filter by availability |

**Sort Values:**
```
price:asc   - Sort by price, lowest first
price:desc  - Sort by price, highest first
name:asc    - Sort by name, A to Z
name:desc   - Sort by name, Z to A
```

**Example Requests:**

```http
# Basic pagination
GET /api/v1/foods?offset=0&limit=20

# With category filter
GET /api/v1/foods?category=Main%20Course&offset=0&limit=20

# With sort
GET /api/v1/foods?sort=price:asc&offset=0&limit=20

# Combined (category + sort + search)
GET /api/v1/foods?category=Beverages&sort=name:asc&search=coffee&offset=0&limit=10
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": 101,
      "name": "Butter Chicken",
      "price": 350.00,
      "restroId": 1,
      "image_url": "https://cdn.smartpos.com/items/butter-chicken.png",
      "category": "Main Course",
      "description": "Creamy tomato-based chicken curry with butter and cream",
      "is_available": true
    },
    {
      "id": 102,
      "name": "Paneer Tikka",
      "price": 250.00,
      "restroId": 1,
      "image_url": "https://cdn.smartpos.com/items/paneer-tikka.png",
      "category": "Appetizers",
      "description": "Grilled cottage cheese marinated in spices",
      "is_available": true
    }
  ],
  "current_page": 0,
  "limit": 20,
  "total": 156,
  "has_more": true
}
```

**Field Specifications:**

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | integer | No | Unique food item ID |
| `name` | string | No | Food item name |
| `price` | decimal(10,2) | No | Item price in currency |
| `restroId` | integer | No | Restaurant ID |
| `image_url` | string | **Yes** | Full URL to food image (can be null) |
| `category` | string | **Yes** | Category name (can be null) |
| `description` | string | **Yes** | Item description (can be null) |
| `is_available` | boolean | No | Availability (default: true) |

**Pagination Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `current_page` | integer | Current page (offset / limit) |
| `limit` | integer | Items per page |
| `total` | integer | Total items available |
| `has_more` | boolean | True if more pages exist |

**Calculation:**
```
current_page = offset / limit
has_more = (total - offset - returned_count) > 0
```

**Error Responses:**

```json
// 400 Bad Request - Invalid parameters
{
  "success": false,
  "error": {
    "code": "INVALID_PARAMETERS",
    "message": "Invalid sort parameter. Use: price:asc, price:desc, name:asc, name:desc"
  }
}

// 404 Not Found - No items
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "No food items found"
  }
}
```

---

### **2. Get Food by ID**

**Priority:** 🟢 MEDIUM

**Endpoint:**
```http
GET /api/v1/foods/{id}
```

**Path Parameters:**
- `id` (integer, required) - Food item ID

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 101,
    "name": "Butter Chicken",
    "price": 350.00,
    "restroId": 1,
    "image_url": "https://cdn.smartpos.com/items/butter-chicken.png",
    "category": "Main Course",
    "description": "Creamy tomato-based chicken curry",
    "is_available": true,
    "preparation_time": 20,
    "ingredients": ["chicken", "butter", "tomato", "cream"],
    "allergens": ["dairy"],
    "calories": 450,
    "created_at": "2026-01-01T00:00:00Z",
    "updated_at": "2026-03-15T12:00:00Z"
  }
}
```

---

### **3. Search Foods**

**Priority:** 🟡 HIGH

**Endpoint:**
```http
GET /api/v1/foods/search
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | string | Yes | Search query |
| `offset` | integer | No | Starting position |
| `limit` | integer | No | Items per page |

**Example:**
```http
GET /api/v1/foods/search?q=chicken&offset=0&limit=10
```

**Response:** Same format as "Get Foods"

**Search Logic:**
- Search in `name` field (case-insensitive)
- Optional: Also search in `description`
- Use LIKE or full-text search

---

## 📂 Categories API

### **1. Get All Categories**

**Priority:** 🟡 HIGH

**Endpoint:**
```http
GET /api/v1/categories
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `restaurant_id` | integer | No | From header | Restaurant ID |
| `is_active` | boolean | No | true | Only active categories |

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Main Course",
      "description": "Main dishes and curries",
      "display_order": 1,
      "is_active": true,
      "item_count": 25,
      "image_url": "https://cdn.smartpos.com/categories/main-course.png"
    },
    {
      "id": 2,
      "name": "Appetizers",
      "description": "Starters and snacks",
      "display_order": 2,
      "is_active": true,
      "item_count": 12,
      "image_url": "https://cdn.smartpos.com/categories/appetizers.png"
    },
    {
      "id": 3,
      "name": "Beverages",
      "description": "Drinks and refreshments",
      "display_order": 3,
      "is_active": true,
      "item_count": 8,
      "image_url": "https://cdn.smartpos.com/categories/beverages.png"
    },
    {
      "id": 4,
      "name": "Desserts",
      "description": "Sweet dishes",
      "display_order": 4,
      "is_active": true,
      "item_count": 6,
      "image_url": "https://cdn.smartpos.com/categories/desserts.png"
    }
  ]
}
```

**Field Specifications:**

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | integer | No | Category ID |
| `name` | string | No | Category name (unique per restaurant) |
| `description` | string | Yes | Category description |
| `display_order` | integer | No | Display order (1-based) |
| `is_active` | boolean | No | Active status |
| `item_count` | integer | No | Number of items in category |
| `image_url` | string | Yes | Category image URL |

---

## 📋 Orders API

### **1. Create Order**

**Priority:** 🟡 HIGH

**Endpoint:**
```http
POST /api/v1/orders
```

**Request Body:**
```json
{
  "table_id": 23,
  "customer_id": null,
  "order_type": "dine_in",
  "notes": "No onions please",
  "items": [
    {
      "menu_item_id": 101,
      "quantity": 2,
      "special_instructions": "Extra spicy",
      "variant_id": null
    },
    {
      "menu_item_id": 102,
      "quantity": 1,
      "special_instructions": null,
      "variant_id": null
    }
  ]
}
```

**Request Field Specifications:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `table_id` | integer | Yes | Table number |
| `customer_id` | integer | No | Customer ID (can be null) |
| `order_type` | string | Yes | "dine_in", "takeaway", or "delivery" |
| `notes` | string | No | Order notes |
| `items` | array | Yes | Array of order items (min: 1) |
| `items[].menu_item_id` | integer | Yes | Food item ID |
| `items[].quantity` | integer | Yes | Quantity (min: 1) |
| `items[].special_instructions` | string | No | Item-specific instructions |
| `items[].variant_id` | integer | No | Variant ID if applicable |

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 5678,
    "order_number": "ORD-2026-03-23-001",
    "table_id": 23,
    "customer_id": null,
    "restaurant_id": 1,
    "order_type": "dine_in",
    "status": "pending",
    "subtotal": 950.00,
    "tax": 171.00,
    "discount": 0.00,
    "total": 1121.00,
    "notes": "No onions please",
    "items": [
      {
        "id": 1,
        "menu_item_id": 101,
        "menu_item_name": "Butter Chicken",
        "quantity": 2,
        "unit_price": 350.00,
        "variant_id": null,
        "variant_name": null,
        "special_instructions": "Extra spicy",
        "subtotal": 700.00
      },
      {
        "id": 2,
        "menu_item_id": 102,
        "menu_item_name": "Paneer Tikka",
        "quantity": 1,
        "unit_price": 250.00,
        "variant_id": null,
        "variant_name": null,
        "special_instructions": null,
        "subtotal": 250.00
      }
    ],
    "created_at": "2026-03-23T10:30:00Z",
    "updated_at": "2026-03-23T10:30:00Z"
  }
}
```

**Calculation Logic:**
```
subtotal = sum(item.unit_price * item.quantity for all items)
tax = subtotal * 0.18  // 18% GST
discount = 0.00  // Apply if discount code used
total = subtotal + tax - discount
```

**Order Status Values:**
- `pending` - Order created, not confirmed
- `confirmed` - Order confirmed by staff
- `preparing` - Kitchen preparing
- `ready` - Ready for serving
- `completed` - Order completed
- `cancelled` - Order cancelled

---

### **2. Get Order by ID**

**Endpoint:**
```http
GET /api/v1/orders/{id}
```

**Response:** Same as Create Order response

---

### **3. Update Order**

**Endpoint:**
```http
PATCH /api/v1/orders/{id}
```

**Request Body (Partial Update):**
```json
{
  "status": "confirmed",
  "notes": "Updated notes",
  "items_to_add": [
    {
      "menu_item_id": 103,
      "quantity": 1
    }
  ],
  "items_to_remove": [1]
}
```

---

## 🧾 Bills API

### **1. Generate Bill**

**Priority:** 🟡 HIGH

**Endpoint:**
```http
POST /api/v1/bills
```

**Request Body:**
```json
{
  "order_id": 5678,
  "discount": {
    "type": "percentage",
    "value": 10.0
  }
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1234,
    "bill_number": "BILL-2026-03-23-001",
    "order_id": 5678,
    "table_id": 23,
    "table_number": "23",
    "restaurant_id": 1,
    "items": [
      {
        "id": 1,
        "menu_item_name": "Butter Chicken",
        "quantity": 2,
        "unit_price": 350.00,
        "variant_name": null,
        "subtotal": 700.00
      },
      {
        "id": 2,
        "menu_item_name": "Paneer Tikka",
        "quantity": 1,
        "unit_price": 250.00,
        "variant_name": null,
        "subtotal": 250.00
      }
    ],
    "subtotal": 950.00,
    "discount": {
      "type": "percentage",
      "value": 10.0,
      "amount": 95.00
    },
    "tax_details": [
      {
        "name": "CGST",
        "rate": 9.0,
        "amount": 76.95
      },
      {
        "name": "SGST",
        "rate": 9.0,
        "amount": 76.95
      }
    ],
    "total_tax": 153.90,
    "total_amount": 1008.90,
    "status": "unpaid",
    "created_at": "2026-03-23T10:45:00Z",
    "printed_at": null
  }
}
```

**Calculation Logic:**
```
subtotal = 950.00
discount_amount = subtotal * (10 / 100) = 95.00
subtotal_after_discount = 950.00 - 95.00 = 855.00
cgst = subtotal_after_discount * 0.09 = 76.95
sgst = subtotal_after_discount * 0.09 = 76.95
total_tax = cgst + sgst = 153.90
total_amount = subtotal_after_discount + total_tax = 1008.90
```

**Bill Status Values:**
- `unpaid` - Bill generated, not paid
- `partial` - Partially paid
- `paid` - Fully paid

---

### **2. Get Bill by ID**

**Endpoint:**
```http
GET /api/v1/bills/{id}
```

**Response:** Same as Generate Bill response

---

## 💳 Payments API

### **1. Process Payment**

**Priority:** 🟢 MEDIUM

**Endpoint:**
```http
POST /api/v1/payments
```

**Headers:**
```http
X-Idempotency-Key: {uuid}  # Required for payments
```

**Request Body:**
```json
{
  "bill_id": 1234,
  "amount": 1008.90,
  "payment_method": "cash",
  "reference_number": null,
  "notes": "Paid in full"
}
```

**Payment Methods:**
- `cash` - Cash payment
- `card` - Card payment
- `upi` - UPI payment
- `wallet` - Digital wallet

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 789,
    "bill_id": 1234,
    "amount": 1008.90,
    "payment_method": "cash",
    "reference_number": null,
    "status": "completed",
    "transaction_id": "TXN-2026-03-23-001",
    "created_at": "2026-03-23T10:50:00Z"
  }
}
```

**Payment Status Values:**
- `pending` - Payment initiated
- `completed` - Payment successful
- `failed` - Payment failed
- `refunded` - Payment refunded

---

## 🪑 Tables API

### **1. Get All Tables**

**Priority:** 🟢 MEDIUM

**Endpoint:**
```http
GET /api/v1/tables
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `restaurant_id` | integer | No | Restaurant ID |
| `status` | string | No | Filter by status |

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "table_number": "1",
      "floor": 1,
      "capacity": 4,
      "status": "available",
      "current_order_id": null,
      "restaurant_id": 1
    },
    {
      "id": 23,
      "table_number": "23",
      "floor": 2,
      "capacity": 6,
      "status": "occupied",
      "current_order_id": 5678,
      "restaurant_id": 1
    }
  ]
}
```

**Table Status Values:**
- `available` - Table is free
- `occupied` - Table has active order
- `reserved` - Table is reserved
- `cleaning` - Being cleaned
- `maintenance` - Under maintenance

---

## 📊 Data Models

### **Food Item Model**

```typescript
interface FoodItem {
  id: number;                    // Required, unique
  name: string;                  // Required, max 255 chars
  price: number;                 // Required, decimal(10,2)
  restroId: number;              // Required
  image_url: string | null;      // Optional, max 500 chars
  category: string | null;       // Optional, max 100 chars
  description: string | null;    // Optional, text
  is_available: boolean;         // Required, default true
}
```

### **Category Model**

```typescript
interface Category {
  id: number;                    // Required, unique
  name: string;                  // Required, max 100 chars, unique per restaurant
  description: string | null;    // Optional
  display_order: number;         // Required, integer
  is_active: boolean;           // Required, default true
  item_count: number;           // Required, calculated field
  image_url: string | null;     // Optional
}
```

### **Order Model**

```typescript
interface Order {
  id: number;
  order_number: string;          // Format: ORD-YYYY-MM-DD-XXX
  table_id: number;
  customer_id: number | null;
  restaurant_id: number;
  order_type: "dine_in" | "takeaway" | "delivery";
  status: OrderStatus;
  subtotal: number;              // decimal(10,2)
  tax: number;                   // decimal(10,2)
  discount: number;              // decimal(10,2)
  total: number;                 // decimal(10,2)
  notes: string | null;
  items: OrderItem[];
  created_at: string;            // ISO 8601 timestamp
  updated_at: string;            // ISO 8601 timestamp
}

type OrderStatus = "pending" | "confirmed" | "preparing" | "ready" | "completed" | "cancelled";

interface OrderItem {
  id: number;
  menu_item_id: number;
  menu_item_name: string;
  quantity: number;
  unit_price: number;
  variant_id: number | null;
  variant_name: string | null;
  special_instructions: string | null;
  subtotal: number;
}
```

### **Bill Model**

```typescript
interface Bill {
  id: number;
  bill_number: string;           // Format: BILL-YYYY-MM-DD-XXX
  order_id: number;
  table_id: number;
  table_number: string;
  restaurant_id: number;
  items: BillItem[];
  subtotal: number;
  discount: Discount;
  tax_details: TaxDetail[];
  total_tax: number;
  total_amount: number;
  status: "unpaid" | "partial" | "paid";
  created_at: string;
  printed_at: string | null;
}

interface BillItem {
  id: number;
  menu_item_name: string;
  quantity: number;
  unit_price: number;
  variant_name: string | null;
  subtotal: number;
}

interface Discount {
  type: "percentage" | "fixed";
  value: number;
  amount: number;               // Calculated discount amount
}

interface TaxDetail {
  name: string;                 // e.g., "CGST", "SGST"
  rate: number;                 // e.g., 9.0 (for 9%)
  amount: number;               // Calculated tax amount
}
```

### **Payment Model**

```typescript
interface Payment {
  id: number;
  bill_id: number;
  amount: number;
  payment_method: "cash" | "card" | "upi" | "wallet";
  reference_number: string | null;
  status: "pending" | "completed" | "failed" | "refunded";
  transaction_id: string;        // Format: TXN-YYYY-MM-DD-XXX
  created_at: string;
}
```

---

## ⚠️ Error Handling

### **Standard Error Codes**

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_CREDENTIALS` | 401 | Invalid username/password |
| `UNAUTHORIZED` | 401 | Missing or invalid token |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `INVALID_PARAMETERS` | 400 | Invalid query/body parameters |
| `VALIDATION_ERROR` | 422 | Validation failed |
| `INTERNAL_ERROR` | 500 | Server error |
| `DATABASE_ERROR` | 500 | Database operation failed |

### **Error Response Format**

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "field": "items",
      "reason": "At least one item is required"
    }
  }
}
```

### **Validation Rules**

**Foods:**
- `name`: Required, max 255 characters
- `price`: Required, must be positive decimal
- `category`: Optional, max 100 characters
- `image_url`: Optional, valid URL format

**Orders:**
- `table_id`: Required, must exist
- `items`: Required, min 1 item
- `items[].quantity`: Required, min 1
- `order_type`: Required, must be one of: dine_in, takeaway, delivery

**Pagination:**
- `offset`: Must be non-negative integer
- `limit`: Must be 1-100
- `sort`: Must match pattern: (price|name):(asc|desc)

---

## 🧪 Testing Checklist

### **Foods API Tests**

- [ ] Get foods with basic pagination
- [ ] Get foods with category filter
- [ ] Get foods with sort (all 4 variations)
- [ ] Get foods with search
- [ ] Get foods with combined filters
- [ ] Verify pagination metadata is correct
- [ ] Test invalid sort parameter returns error
- [ ] Test limit > 100 returns error or caps at 100
- [ ] Test negative offset returns error
- [ ] Verify null optional fields are handled

### **Categories API Tests**

- [ ] Get all categories
- [ ] Verify item_count is correct per category
- [ ] Verify categories ordered by display_order
- [ ] Test filtering by is_active

### **Orders API Tests**

- [ ] Create order with valid data
- [ ] Verify order_number is generated correctly
- [ ] Verify subtotal, tax, total calculations
- [ ] Test creating order with empty items returns error
- [ ] Test creating order with invalid table_id returns error
- [ ] Get order by ID
- [ ] Update order status
- [ ] Add items to existing order
- [ ] Remove items from order

### **Bills API Tests**

- [ ] Generate bill from order
- [ ] Verify bill_number is generated correctly
- [ ] Test discount calculations (percentage and fixed)
- [ ] Verify tax calculations (CGST + SGST)
- [ ] Verify total_amount calculation
- [ ] Get bill by ID
- [ ] Test duplicate bill generation returns existing bill

### **Payments API Tests**

- [ ] Process cash payment
- [ ] Process card payment
- [ ] Verify transaction_id is generated
- [ ] Test idempotency (same key returns same response)
- [ ] Test payment amount must match bill amount
- [ ] Test payment for non-existent bill returns error

### **Integration Tests**

- [ ] Complete flow: Create order → Generate bill → Process payment
- [ ] Verify table status updates after order creation
- [ ] Verify order status updates after payment
- [ ] Verify bill status updates after payment

---

## 📈 Performance Requirements

### **Response Time Targets**

| Endpoint | Target | Max Acceptable |
|----------|--------|----------------|
| GET /foods | 200ms | 500ms |
| POST /orders | 300ms | 1000ms |
| POST /bills | 200ms | 800ms |
| POST /payments | 500ms | 2000ms |

### **Optimization Recommendations**

**Database:**
- Add index on `foods(category)`
- Add index on `foods(is_available)`
- Add index on `foods(price)`
- Add index on `foods(name)` for search
- Add index on `orders(table_id)`
- Add index on `orders(status)`

**Caching:**
- Cache categories (rarely change)
- Cache food items with 5-minute TTL
- Invalidate cache on updates

**Pagination:**
- Use offset/limit for pagination
- Return metadata (total, has_more)
- Limit max page size to 100

---

## 🚀 Implementation Priority

### **Phase 1: MVP (Week 1)** 🔴

**Must implement:**
1. ✅ GET /api/v1/foods (with category filter, sort, search)
2. ✅ GET /api/v1/categories
3. ✅ POST /api/v1/auth/login

**Effort:** 6-8 hours

### **Phase 2: Orders (Week 2)** 🟡

**Implement:**
4. ✅ POST /api/v1/orders
5. ✅ GET /api/v1/orders/{id}
6. ✅ PATCH /api/v1/orders/{id}

**Effort:** 8-10 hours

### **Phase 3: Billing & Payments (Week 3)** 🟢

**Implement:**
7. ✅ POST /api/v1/bills
8. ✅ POST /api/v1/payments

**Effort:** 6-8 hours

---

## 📦 Deliverables

**Backend team must provide:**

1. **Working API endpoints** on staging server
2. **Postman collection** with example requests
3. **Database schema** (SQL scripts)
4. **API documentation** (Swagger/OpenAPI)
5. **Staging URL** for Android team testing
6. **Test credentials** for authentication

---

## 🎯 Success Criteria

**API is complete when:**

- ✅ All endpoints return correct status codes
- ✅ Response formats match specifications exactly
- ✅ All query parameters work correctly
- ✅ Pagination metadata is accurate
- ✅ Error responses follow standard format
- ✅ Authentication works
- ✅ All calculations are correct (subtotal, tax, total)
- ✅ Database constraints are enforced
- ✅ Performance targets are met
- ✅ Android team confirms integration works

---

## 📞 Contact & Support

**Android Team Lead:** [Your Name]  
**Questions:** Share via Slack/Email  
**API Testing:** Postman collection will be provided by backend

---

## 📝 Notes for Backend Team

### **Key Points:**

1. **All new fields are optional** (image_url, category, description)
    - Can be null in database
    - Android app handles null gracefully
    - No breaking changes to existing code

2. **Pagination is offset-based** (not page-based)
    - offset=0, limit=20 for page 1
    - offset=20, limit=20 for page 2
    - Calculate has_more correctly

3. **Category filter is case-insensitive**
    - "main course", "Main Course", "MAIN COURSE" should all work

4. **Sort parameter validation**
    - Only accept: price:asc, price:desc, name:asc, name:desc
    - Return error for invalid values

5. **Tax calculation uses 18% GST**
    - Split as 9% CGST + 9% SGST
    - Apply on subtotal after discount

6. **Generate unique numbers**
    - order_number: ORD-YYYY-MM-DD-XXX
    - bill_number: BILL-YYYY-MM-DD-XXX
    - transaction_id: TXN-YYYY-MM-DD-XXX

---

## ✅ Ready to Implement!

**This document contains 100% of what you need to implement the backend API from scratch.**

**Estimated Total Effort:** 20-26 hours  
**Timeline:** 2-3 weeks  
**Team Size:** 1-2 backend developers

**Android app is waiting and ready to integrate as soon as Phase 1 (MVP) is complete!** 🚀

---

**Last Updated:** March 23, 2026  
**Version:** 1.1  
**Status:** 🔴 Ready for Backend Implementation


3. [Authentication](#authentication)
4. [Foods API](#foods-api)
5. [Categories API](#categories-api)
6. [Orders API](#orders-api)
7. [Bills API](#bills-api)
8. [Payments API](#payments-api)
9. [Tables API](#tables-api)
10. [Data Models](#data-models)
11. [Error Handling](#error-handling)
12. [Testing Checklist](#testing-checklist)

---

## 🎯 Overview

### **What This Document Contains**

Complete specifications for **ALL** APIs needed for SmartPos Android app, including:
- Exact request/response formats
- All query parameters
- Data models/schemas
- Error responses
- Authentication
- Examples for every endpoint

### **Backend Implementation Scope**

**Total Endpoints:** 8 (for MVP - Foods, Categories, Orders, Bills, Payments, Tables)

**Priority Breakdown:**
- 🔴 **CRITICAL** (3 endpoints) - Must implement first
- 🟡 **HIGH** (3 endpoints) - Implement next
- 🟢 **MEDIUM** (2 endpoints) - Can be implemented later

---

## 🌐 Base Configuration

### **Base URLs**

```
Development:  http://localhost:8080/api/v1
Staging:      https://staging-api.smartpos.com/api/v1
Production:   https://api.smartpos.com/api/v1
```

### **Standard Headers**

**All Requests Must Include:**
```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer {access_token}
X-Restaurant-ID: {restaurant_id}
```

**Optional Headers:**
```http
X-Request-ID: {uuid}              # For request tracing
X-Idempotency-Key: {uuid}         # For critical operations (payments)
```

### **Response Format (Standard Wrapper)**

**Success Response:**
```json
{
  "success": true,
  "data": { ... },
  "message": "Optional success message"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": { ... }
  }
}
```

---

## 🔐 Authentication

### **1. Login**

**Priority:** 🔴 CRITICAL

**Endpoint:**
```http
POST /api/v1/auth/login
```

**Request Body:**
```json
{
  "username": "admin@restaurant.com",
  "password": "SecurePassword123!",
  "device_id": "tablet-001",
  "device_type": "tablet"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGci...",
    "refresh_token": "eyJhbGci...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "user": {
      "id": 1,
      "username": "admin@restaurant.com",
      "first_name": "Admin",
      "last_name": "User",
      "role": "admin",
      "restaurant_id": 1,
      "permissions": ["order.*", "bill.*", "payment.*", "menu.*"]
    }
  }
}
```

**Error Responses:**
```json
// 401 Unauthorized
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid username or password"
  }
}

// 403 Forbidden
{
  "success": false,
  "error": {
    "code": "ACCOUNT_DISABLED",
    "message": "Account has been disabled"
  }
}
```

---

## 🍽️ Foods API

### **1. Get Foods (Paginated with Filters)** 

**Priority:** 🔴 CRITICAL - **IMPLEMENT THIS FIRST!**

**Endpoint:**
```http
GET /api/v1/foods
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `offset` | integer | No | 0 | Starting position (0-based) |
| `limit` | integer | No | 20 | Items per page (max: 100) |
| `category` | string | No | null | Filter by exact category name |
| `sort` | string | No | null | Sort order (see below) |
| `search` | string | No | null | Search in item name |
| `restaurant_id` | integer | No | From header | Restaurant ID |
| `is_available` | boolean | No | null | Filter by availability |

**Sort Values:**
```
price:asc   - Sort by price, lowest first
price:desc  - Sort by price, highest first
name:asc    - Sort by name, A to Z
name:desc   - Sort by name, Z to A
```

**Example Requests:**

```http
# Basic pagination
GET /api/v1/foods?offset=0&limit=20

# With category filter
GET /api/v1/foods?category=Main%20Course&offset=0&limit=20

# With sort
GET /api/v1/foods?sort=price:asc&offset=0&limit=20

# Combined (category + sort + search)
GET /api/v1/foods?category=Beverages&sort=name:asc&search=coffee&offset=0&limit=10
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": 101,
      "name": "Butter Chicken",
      "price": 350.00,
      "restroId": 1,
      "image_url": "https://cdn.smartpos.com/items/butter-chicken.png",
      "category": "Main Course",
      "description": "Creamy tomato-based chicken curry with butter and cream",
      "is_available": true
    },
    {
      "id": 102,
      "name": "Paneer Tikka",
      "price": 250.00,
      "restroId": 1,
      "image_url": "https://cdn.smartpos.com/items/paneer-tikka.png",
      "category": "Appetizers",
      "description": "Grilled cottage cheese marinated in spices",
      "is_available": true
    }
  ],
  "current_page": 0,
  "limit": 20,
  "total": 156,
  "has_more": true
}
```

**Field Specifications:**

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | integer | No | Unique food item ID |
| `name` | string | No | Food item name |
| `price` | decimal(10,2) | No | Item price in currency |
| `restroId` | integer | No | Restaurant ID |
| `image_url` | string | **Yes** | Full URL to food image (can be null) |
| `category` | string | **Yes** | Category name (can be null) |
| `description` | string | **Yes** | Item description (can be null) |
| `is_available` | boolean | No | Availability (default: true) |

**Pagination Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `current_page` | integer | Current page (offset / limit) |
| `limit` | integer | Items per page |
| `total` | integer | Total items available |
| `has_more` | boolean | True if more pages exist |

**Calculation:**
```
current_page = offset / limit
has_more = (total - offset - returned_count) > 0
```

**Error Responses:**

```json
// 400 Bad Request - Invalid parameters
{
  "success": false,
  "error": {
    "code": "INVALID_PARAMETERS",
    "message": "Invalid sort parameter. Use: price:asc, price:desc, name:asc, name:desc"
  }
}

// 404 Not Found - No items
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "No food items found"
  }
}
```

---

### **2. Get Food by ID**

**Priority:** 🟢 MEDIUM

**Endpoint:**
```http
GET /api/v1/foods/{id}
```

**Path Parameters:**
- `id` (integer, required) - Food item ID

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 101,
    "name": "Butter Chicken",
    "price": 350.00,
    "restroId": 1,
    "image_url": "https://cdn.smartpos.com/items/butter-chicken.png",
    "category": "Main Course",
    "description": "Creamy tomato-based chicken curry",
    "is_available": true,
    "preparation_time": 20,
    "ingredients": ["chicken", "butter", "tomato", "cream"],
    "allergens": ["dairy"],
    "calories": 450,
    "created_at": "2026-01-01T00:00:00Z",
    "updated_at": "2026-03-15T12:00:00Z"
  }
}
```

---

### **3. Search Foods**

**Priority:** 🟡 HIGH

**Endpoint:**
```http
GET /api/v1/foods/search
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | string | Yes | Search query |
| `offset` | integer | No | Starting position |
| `limit` | integer | No | Items per page |

**Example:**
```http
GET /api/v1/foods/search?q=chicken&offset=0&limit=10
```

**Response:** Same format as "Get Foods"

**Search Logic:**
- Search in `name` field (case-insensitive)
- Optional: Also search in `description`
- Use LIKE or full-text search

---

## 📂 Categories API

### **1. Get All Categories**

**Priority:** 🟡 HIGH

**Endpoint:**
```http
GET /api/v1/categories
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `restaurant_id` | integer | No | From header | Restaurant ID |
| `is_active` | boolean | No | true | Only active categories |

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Main Course",
      "description": "Main dishes and curries",
      "display_order": 1,
      "is_active": true,
      "item_count": 25,
      "image_url": "https://cdn.smartpos.com/categories/main-course.png"
    },
    {
      "id": 2,
      "name": "Appetizers",
      "description": "Starters and snacks",
      "display_order": 2,
      "is_active": true,
      "item_count": 12,
      "image_url": "https://cdn.smartpos.com/categories/appetizers.png"
    },
    {
      "id": 3,
      "name": "Beverages",
      "description": "Drinks and refreshments",
      "display_order": 3,
      "is_active": true,
      "item_count": 8,
      "image_url": "https://cdn.smartpos.com/categories/beverages.png"
    },
    {
      "id": 4,
      "name": "Desserts",
      "description": "Sweet dishes",
      "display_order": 4,
      "is_active": true,
      "item_count": 6,
      "image_url": "https://cdn.smartpos.com/categories/desserts.png"
    }
  ]
}
```

**Field Specifications:**

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | integer | No | Category ID |
| `name` | string | No | Category name (unique per restaurant) |
| `description` | string | Yes | Category description |
| `display_order` | integer | No | Display order (1-based) |
| `is_active` | boolean | No | Active status |
| `item_count` | integer | No | Number of items in category |
| `image_url` | string | Yes | Category image URL |

---

## 📋 Orders API

### **1. Create Order**

**Priority:** 🟡 HIGH

**Endpoint:**
```http
POST /api/v1/orders
```

**Request Body:**
```json
{
  "table_id": 23,
  "customer_id": null,
  "order_type": "dine_in",
  "notes": "No onions please",
  "items": [
    {
      "menu_item_id": 101,
      "quantity": 2,
      "special_instructions": "Extra spicy",
      "variant_id": null
    },
    {
      "menu_item_id": 102,
      "quantity": 1,
      "special_instructions": null,
      "variant_id": null
    }
  ]
}
```

**Request Field Specifications:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `table_id` | integer | Yes | Table number |
| `customer_id` | integer | No | Customer ID (can be null) |
| `order_type` | string | Yes | "dine_in", "takeaway", or "delivery" |
| `notes` | string | No | Order notes |
| `items` | array | Yes | Array of order items (min: 1) |
| `items[].menu_item_id` | integer | Yes | Food item ID |
| `items[].quantity` | integer | Yes | Quantity (min: 1) |
| `items[].special_instructions` | string | No | Item-specific instructions |
| `items[].variant_id` | integer | No | Variant ID if applicable |

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 5678,
    "order_number": "ORD-2026-03-23-001",
    "table_id": 23,
    "customer_id": null,
    "restaurant_id": 1,
    "order_type": "dine_in",
    "status": "pending",
    "subtotal": 950.00,
    "tax": 171.00,
    "discount": 0.00,
    "total": 1121.00,
    "notes": "No onions please",
    "items": [
      {
        "id": 1,
        "menu_item_id": 101,
        "menu_item_name": "Butter Chicken",
        "quantity": 2,
        "unit_price": 350.00,
        "variant_id": null,
        "variant_name": null,
        "special_instructions": "Extra spicy",
        "subtotal": 700.00
      },
      {
        "id": 2,
        "menu_item_id": 102,
        "menu_item_name": "Paneer Tikka",
        "quantity": 1,
        "unit_price": 250.00,
        "variant_id": null,
        "variant_name": null,
        "special_instructions": null,
        "subtotal": 250.00
      }
    ],
    "created_at": "2026-03-23T10:30:00Z",
    "updated_at": "2026-03-23T10:30:00Z"
  }
}
```

**Calculation Logic:**
```
subtotal = sum(item.unit_price * item.quantity for all items)
tax = subtotal * 0.18  // 18% GST
discount = 0.00  // Apply if discount code used
total = subtotal + tax - discount
```

**Order Status Values:**
- `pending` - Order created, not confirmed
- `confirmed` - Order confirmed by staff
- `preparing` - Kitchen preparing
- `ready` - Ready for serving
- `completed` - Order completed
- `cancelled` - Order cancelled

---

### **2. Get Order by ID**

**Endpoint:**
```http
GET /api/v1/orders/{id}
```

**Response:** Same as Create Order response

---

### **3. Update Order**

**Endpoint:**
```http
PATCH /api/v1/orders/{id}
```

**Request Body (Partial Update):**
```json
{
  "status": "confirmed",
  "notes": "Updated notes",
  "items_to_add": [
    {
      "menu_item_id": 103,
      "quantity": 1
    }
  ],
  "items_to_remove": [1]
}
```

---

## 🧾 Bills API

### **1. Generate Bill**

**Priority:** 🟡 HIGH

**Endpoint:**
```http
POST /api/v1/bills
```

**Request Body:**
```json
{
  "order_id": 5678,
  "discount": {
    "type": "percentage",
    "value": 10.0
  }
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1234,
    "bill_number": "BILL-2026-03-23-001",
    "order_id": 5678,
    "table_id": 23,
    "table_number": "23",
    "restaurant_id": 1,
    "items": [
      {
        "id": 1,
        "menu_item_name": "Butter Chicken",
        "quantity": 2,
        "unit_price": 350.00,
        "variant_name": null,
        "subtotal": 700.00
      },
      {
        "id": 2,
        "menu_item_name": "Paneer Tikka",
        "quantity": 1,
        "unit_price": 250.00,
        "variant_name": null,
        "subtotal": 250.00
      }
    ],
    "subtotal": 950.00,
    "discount": {
      "type": "percentage",
      "value": 10.0,
      "amount": 95.00
    },
    "tax_details": [
      {
        "name": "CGST",
        "rate": 9.0,
        "amount": 76.95
      },
      {
        "name": "SGST",
        "rate": 9.0,
        "amount": 76.95
      }
    ],
    "total_tax": 153.90,
    "total_amount": 1008.90,
    "status": "unpaid",
    "created_at": "2026-03-23T10:45:00Z",
    "printed_at": null
  }
}
```

**Calculation Logic:**
```
subtotal = 950.00
discount_amount = subtotal * (10 / 100) = 95.00
subtotal_after_discount = 950.00 - 95.00 = 855.00
cgst = subtotal_after_discount * 0.09 = 76.95
sgst = subtotal_after_discount * 0.09 = 76.95
total_tax = cgst + sgst = 153.90
total_amount = subtotal_after_discount + total_tax = 1008.90
```

**Bill Status Values:**
- `unpaid` - Bill generated, not paid
- `partial` - Partially paid
- `paid` - Fully paid

---

### **2. Get Bill by ID**

**Endpoint:**
```http
GET /api/v1/bills/{id}
```

**Response:** Same as Generate Bill response

---

## 💳 Payments API

### **1. Process Payment**

**Priority:** 🟢 MEDIUM

**Endpoint:**
```http
POST /api/v1/payments
```

**Headers:**
```http
X-Idempotency-Key: {uuid}  # Required for payments
```

**Request Body:**
```json
{
  "bill_id": 1234,
  "amount": 1008.90,
  "payment_method": "cash",
  "reference_number": null,
  "notes": "Paid in full"
}
```

**Payment Methods:**
- `cash` - Cash payment
- `card` - Card payment
- `upi` - UPI payment
- `wallet` - Digital wallet

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 789,
    "bill_id": 1234,
    "amount": 1008.90,
    "payment_method": "cash",
    "reference_number": null,
    "status": "completed",
    "transaction_id": "TXN-2026-03-23-001",
    "created_at": "2026-03-23T10:50:00Z"
  }
}
```

**Payment Status Values:**
- `pending` - Payment initiated
- `completed` - Payment successful
- `failed` - Payment failed
- `refunded` - Payment refunded

---

## 🪑 Tables API

### **1. Get All Tables**

**Priority:** 🟢 MEDIUM

**Endpoint:**
```http
GET /api/v1/tables
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `restaurant_id` | integer | No | Restaurant ID |
| `status` | string | No | Filter by status |

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "table_number": "1",
      "floor": 1,
      "capacity": 4,
      "status": "available",
      "current_order_id": null,
      "restaurant_id": 1
    },
    {
      "id": 23,
      "table_number": "23",
      "floor": 2,
      "capacity": 6,
      "status": "occupied",
      "current_order_id": 5678,
      "restaurant_id": 1
    }
  ]
}
```

**Table Status Values:**
- `available` - Table is free
- `occupied` - Table has active order
- `reserved` - Table is reserved
- `cleaning` - Being cleaned
- `maintenance` - Under maintenance

---

## 📊 Data Models

### **Food Item Model**

```typescript
interface FoodItem {
  id: number;                    // Required, unique
  name: string;                  // Required, max 255 chars
  price: number;                 // Required, decimal(10,2)
  restroId: number;              // Required
  image_url: string | null;      // Optional, max 500 chars
  category: string | null;       // Optional, max 100 chars
  description: string | null;    // Optional, text
  is_available: boolean;         // Required, default true
}
```

### **Category Model**

```typescript
interface Category {
  id: number;                    // Required, unique
  name: string;                  // Required, max 100 chars, unique per restaurant
  description: string | null;    // Optional
  display_order: number;         // Required, integer
  is_active: boolean;           // Required, default true
  item_count: number;           // Required, calculated field
  image_url: string | null;     // Optional
}
```

### **Order Model**

```typescript
interface Order {
  id: number;
  order_number: string;          // Format: ORD-YYYY-MM-DD-XXX
  table_id: number;
  customer_id: number | null;
  restaurant_id: number;
  order_type: "dine_in" | "takeaway" | "delivery";
  status: OrderStatus;
  subtotal: number;              // decimal(10,2)
  tax: number;                   // decimal(10,2)
  discount: number;              // decimal(10,2)
  total: number;                 // decimal(10,2)
  notes: string | null;
  items: OrderItem[];
  created_at: string;            // ISO 8601 timestamp
  updated_at: string;            // ISO 8601 timestamp
}

type OrderStatus = "pending" | "confirmed" | "preparing" | "ready" | "completed" | "cancelled";

interface OrderItem {
  id: number;
  menu_item_id: number;
  menu_item_name: string;
  quantity: number;
  unit_price: number;
  variant_id: number | null;
  variant_name: string | null;
  special_instructions: string | null;
  subtotal: number;
}
```

### **Bill Model**

```typescript
interface Bill {
  id: number;
  bill_number: string;           // Format: BILL-YYYY-MM-DD-XXX
  order_id: number;
  table_id: number;
  table_number: string;
  restaurant_id: number;
  items: BillItem[];
  subtotal: number;
  discount: Discount;
  tax_details: TaxDetail[];
  total_tax: number;
  total_amount: number;
  status: "unpaid" | "partial" | "paid";
  created_at: string;
  printed_at: string | null;
}

interface BillItem {
  id: number;
  menu_item_name: string;
  quantity: number;
  unit_price: number;
  variant_name: string | null;
  subtotal: number;
}

interface Discount {
  type: "percentage" | "fixed";
  value: number;
  amount: number;               // Calculated discount amount
}

interface TaxDetail {
  name: string;                 // e.g., "CGST", "SGST"
  rate: number;                 // e.g., 9.0 (for 9%)
  amount: number;               // Calculated tax amount
}
```

### **Payment Model**

```typescript
interface Payment {
  id: number;
  bill_id: number;
  amount: number;
  payment_method: "cash" | "card" | "upi" | "wallet";
  reference_number: string | null;
  status: "pending" | "completed" | "failed" | "refunded";
  transaction_id: string;        // Format: TXN-YYYY-MM-DD-XXX
  created_at: string;
}
```

---

## ⚠️ Error Handling

### **Standard Error Codes**

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_CREDENTIALS` | 401 | Invalid username/password |
| `UNAUTHORIZED` | 401 | Missing or invalid token |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `INVALID_PARAMETERS` | 400 | Invalid query/body parameters |
| `VALIDATION_ERROR` | 422 | Validation failed |
| `INTERNAL_ERROR` | 500 | Server error |
| `DATABASE_ERROR` | 500 | Database operation failed |

### **Error Response Format**

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "field": "items",
      "reason": "At least one item is required"
    }
  }
}
```

### **Validation Rules**

**Foods:**
- `name`: Required, max 255 characters
- `price`: Required, must be positive decimal
- `category`: Optional, max 100 characters
- `image_url`: Optional, valid URL format

**Orders:**
- `table_id`: Required, must exist
- `items`: Required, min 1 item
- `items[].quantity`: Required, min 1
- `order_type`: Required, must be one of: dine_in, takeaway, delivery

**Pagination:**
- `offset`: Must be non-negative integer
- `limit`: Must be 1-100
- `sort`: Must match pattern: (price|name):(asc|desc)

---

## 🧪 Testing Checklist

### **Foods API Tests**

- [ ] Get foods with basic pagination
- [ ] Get foods with category filter
- [ ] Get foods with sort (all 4 variations)
- [ ] Get foods with search
- [ ] Get foods with combined filters
- [ ] Verify pagination metadata is correct
- [ ] Test invalid sort parameter returns error
- [ ] Test limit > 100 returns error or caps at 100
- [ ] Test negative offset returns error
- [ ] Verify null optional fields are handled

### **Categories API Tests**

- [ ] Get all categories
- [ ] Verify item_count is correct per category
- [ ] Verify categories ordered by display_order
- [ ] Test filtering by is_active

### **Orders API Tests**

- [ ] Create order with valid data
- [ ] Verify order_number is generated correctly
- [ ] Verify subtotal, tax, total calculations
- [ ] Test creating order with empty items returns error
- [ ] Test creating order with invalid table_id returns error
- [ ] Get order by ID
- [ ] Update order status
- [ ] Add items to existing order
- [ ] Remove items from order

### **Bills API Tests**

- [ ] Generate bill from order
- [ ] Verify bill_number is generated correctly
- [ ] Test discount calculations (percentage and fixed)
- [ ] Verify tax calculations (CGST + SGST)
- [ ] Verify total_amount calculation
- [ ] Get bill by ID
- [ ] Test duplicate bill generation returns existing bill

### **Payments API Tests**

- [ ] Process cash payment
- [ ] Process card payment
- [ ] Verify transaction_id is generated
- [ ] Test idempotency (same key returns same response)
- [ ] Test payment amount must match bill amount
- [ ] Test payment for non-existent bill returns error

### **Integration Tests**

- [ ] Complete flow: Create order → Generate bill → Process payment
- [ ] Verify table status updates after order creation
- [ ] Verify order status updates after payment
- [ ] Verify bill status updates after payment

---

## 📈 Performance Requirements

### **Response Time Targets**

| Endpoint | Target | Max Acceptable |
|----------|--------|----------------|
| GET /foods | 200ms | 500ms |
| POST /orders | 300ms | 1000ms |
| POST /bills | 200ms | 800ms |
| POST /payments | 500ms | 2000ms |

### **Optimization Recommendations**

**Database:**
- Add index on `foods(category)`
- Add index on `foods(is_available)`
- Add index on `foods(price)`
- Add index on `foods(name)` for search
- Add index on `orders(table_id)`
- Add index on `orders(status)`

**Caching:**
- Cache categories (rarely change)
- Cache food items with 5-minute TTL
- Invalidate cache on updates

**Pagination:**
- Use offset/limit for pagination
- Return metadata (total, has_more)
- Limit max page size to 100

---

## 🚀 Implementation Priority

### **Phase 1: MVP (Week 1)** 🔴

**Must implement:**
1. ✅ GET /api/v1/foods (with category filter, sort, search)
2. ✅ GET /api/v1/categories
3. ✅ POST /api/v1/auth/login

**Effort:** 6-8 hours

### **Phase 2: Orders (Week 2)** 🟡

**Implement:**
4. ✅ POST /api/v1/orders
5. ✅ GET /api/v1/orders/{id}
6. ✅ PATCH /api/v1/orders/{id}

**Effort:** 8-10 hours

### **Phase 3: Billing & Payments (Week 3)** 🟢

**Implement:**
7. ✅ POST /api/v1/bills
8. ✅ POST /api/v1/payments

**Effort:** 6-8 hours

---

## 📦 Deliverables

**Backend team must provide:**

1. **Working API endpoints** on staging server
2. **Postman collection** with example requests
3. **Database schema** (SQL scripts)
4. **API documentation** (Swagger/OpenAPI)
5. **Staging URL** for Android team testing
6. **Test credentials** for authentication

---

## 🎯 Success Criteria

**API is complete when:**

- ✅ All endpoints return correct status codes
- ✅ Response formats match specifications exactly
- ✅ All query parameters work correctly
- ✅ Pagination metadata is accurate
- ✅ Error responses follow standard format
- ✅ Authentication works
- ✅ All calculations are correct (subtotal, tax, total)
- ✅ Database constraints are enforced
- ✅ Performance targets are met
- ✅ Android team confirms integration works

---

## 📞 Contact & Support

**Android Team Lead:** [Your Name]  
**Questions:** Share via Slack/Email  
**API Testing:** Postman collection will be provided by backend

---

## 📝 Notes for Backend Team

### **Key Points:**

1. **All new fields are optional** (image_url, category, description)
   - Can be null in database
   - Android app handles null gracefully
   - No breaking changes to existing code

2. **Pagination is offset-based** (not page-based)
   - offset=0, limit=20 for page 1
   - offset=20, limit=20 for page 2
   - Calculate has_more correctly

3. **Category filter is case-insensitive**
   - "main course", "Main Course", "MAIN COURSE" should all work

4. **Sort parameter validation**
   - Only accept: price:asc, price:desc, name:asc, name:desc
   - Return error for invalid values

5. **Tax calculation uses 18% GST**
   - Split as 9% CGST + 9% SGST
   - Apply on subtotal after discount

6. **Generate unique numbers**
   - order_number: ORD-YYYY-MM-DD-XXX
   - bill_number: BILL-YYYY-MM-DD-XXX
   - transaction_id: TXN-YYYY-MM-DD-XXX

---

## ✅ Ready to Implement!

**This document contains 100% of what you need to implement the backend API from scratch.**

**Estimated Total Effort:** 20-26 hours  
**Timeline:** 2-3 weeks  
**Team Size:** 1-2 backend developers  

**Android app is waiting and ready to integrate as soon as Phase 1 (MVP) is complete!** 🚀

---

**Last Updated:** March 23, 2026  
**Version:** 1.1  
**Status:** 🔴 Ready for Backend Implementation

