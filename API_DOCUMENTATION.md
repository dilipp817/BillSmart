# BillSmart Backend — Complete API Documentation

**Base URL:** `http://localhost:8080`  
**API Version:** v1  
**Date:** April 4, 2026 (updated after mobile team review)  
**Prefix:** All endpoints start with `/api/v1/`

---

## 📌 Standard Response Format

Every API response is wrapped in this envelope:

```json
{
  "success": true,
  "message": "Human readable message",
  "data": { ... },
  "error": null
}
```

### Error Response
```json
{
  "success": false,
  "message": null,
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Order not found with ID: 5",
    "details": {}
  }
}
```

### Error Codes
| Code | HTTP Status | Meaning |
|------|-------------|---------|
| `RESOURCE_NOT_FOUND` | 404 | Record doesn't exist |
| `VALIDATION_ERROR` | 400 | Input validation failed |
| `CONFLICT` | 409 | Duplicate or state conflict |
| `UNAUTHORIZED` | 401 | Missing or invalid token |
| `FORBIDDEN` | 403 | Not allowed |
| `INTERNAL_ERROR` | 500 | Server error |

---

## 🔐 Authentication

### POST `/api/v1/auth/login`
Login and get JWT token.

**Request Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Validation:**
- `username` — required, 3–50 characters
- `password` — required, 6–100 characters

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@restaurant.com",
    "role": "ADMIN",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

> `expiresIn` is in seconds (86400 = 24 hours)

---

### GET `/api/v1/auth/me`
Get current logged-in user info.

**Request Header:**
```
Authorization: Bearer <token>
```

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "User info retrieved",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@restaurant.com",
    "role": "ADMIN",
    "isActive": true
  }
}
```

---

### POST `/api/v1/auth/validate`
Validate a JWT token and return user info if valid.

**Query Param:** `?token=<jwt_token>`

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Token is valid",
  "data": {
    "valid": true,
    "username": "admin",
    "userId": 1,
    "role": "staff"
  }
}
```

**Error Response `401 UNAUTHORIZED`:**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "Token is invalid or expired"
  }
}
```

---

## 🍽️ Foods

### GET `/api/v1/foods`
Get all foods with pagination, filtering, and sorting.

**Query Parameters:**
| Param | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `restaurantId` | Long | No | — | Filter by restaurant |
| `categoryId` | Long | No | — | Filter by category |
| `search` | String | No | — | Search in food name |
| `isVegetarian` | Boolean | No | — | Filter vegetarian items |
| `isSpicy` | Boolean | No | — | Filter spicy items |
| `sort` | String | No | `id:asc` | Sort: `price:asc`, `price:desc`, `name:asc`, `name:desc` |
| `offset` | Int | No | `0` | Pagination start position |
| `limit` | Int | No | `20` | Items per page (max 100) |

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Foods retrieved successfully",
  "data": {
    "data": [
      {
        "id": 1,
        "name": "Margherita Pizza",
        "price": 299.0,
        "imageUrl": "https://cdn.example.com/pizza.jpg",
        "categoryName": "Pizza",
        "isAvailable": true,
        "isVegetarian": true,
        "isSpicy": false
      }
    ],
    "pagination": {
      "currentPage": 0,
      "limit": 20,
      "total": 45,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

---

### GET `/api/v1/foods/search`
Search foods by keyword with filters. Database-level search (efficient).

**Query Parameters:**
| Param | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `q` | String | No | — | Keyword — searches food name (case-insensitive) |
| `restaurantId` | Long | No | — | Filter by restaurant |
| `categoryId` | Long | No | — | Filter by category |
| `isVegetarian` | Boolean | No | — | Filter vegetarian |
| `isSpicy` | Boolean | No | — | Filter spicy |
| `isAvailable` | Boolean | No | — | Filter by availability |
| `offset` | Int | No | `0` | Pagination offset |
| `limit` | Int | No | `20` | Page size (max 100) |

**Example:** `GET /api/v1/foods/search?q=pizza&restaurantId=1&isVegetarian=true`

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "data": [
      {
        "id": 1,
        "name": "Margherita Pizza",
        "price": 299.0,
        "imageUrl": "https://cdn.example.com/pizza.jpg",
        "categoryName": "Pizza",
        "isAvailable": true,
        "isVegetarian": true,
        "isSpicy": false
      }
    ],
    "pagination": {
      "currentPage": 0,
      "limit": 20,
      "total": 3,
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false
    }
  }
}
```

---

### GET `/api/v1/foods/{id}`
Get a single food item by ID.

**Path Param:** `id` — Food ID

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Food retrieved successfully",
  "data": {
    "id": 1,
    "name": "Margherita Pizza",
    "price": 299.0,
    "description": "Classic tomato and mozzarella pizza",
    "imageUrl": "https://cdn.example.com/pizza.jpg",
    "categoryId": 2,
    "categoryName": "Pizza",
    "restaurantId": 1,
    "restaurantName": "Pizza Palace",
    "isAvailable": true,
    "preparationTime": 15,
    "allergens": "Gluten, Dairy",
    "calories": 720,
    "isVegetarian": true,
    "isSpicy": false,
    "createdAt": "2026-03-28T10:00:00",
    "updatedAt": "2026-03-28T10:00:00"
  }
}
```

---

### GET `/api/v1/foods/restaurant/{restaurantId}`
Get all foods for a specific restaurant (paginated).

**Path Param:** `restaurantId`  
**Query Params:** `page` (default 0), `limit` (default 20)

**Response:** Same paginated format as `GET /api/v1/foods`

---

### POST `/api/v1/foods/restaurant/{restaurantId}`
Create a new food item for a restaurant.

**Path Param:** `restaurantId`

**Request Body:**
```json
{
  "name": "Margherita Pizza",
  "price": 299.0,
  "description": "Classic tomato and mozzarella pizza",
  "imageUrl": "https://cdn.example.com/pizza.jpg",
  "categoryId": 2,
  "isVegetarian": true,
  "isSpicy": false
}
```

**Validation:**
- `name` — required
- `price` — required, must be > 0, max 999999.99

**Success Response `201 Created`:**
```json
{
  "success": true,
  "message": "Food created successfully",
  "data": { /* full FoodResponse */ }
}
```

---

## 📂 Categories

### GET `/api/v1/categories`
Get all categories for a restaurant.

**Query Param:** `?restaurantId=1` (required)

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Pizza",
      "description": "All types of pizza",
      "imageUrl": "https://cdn.example.com/pizza-cat.jpg",
      "displayOrder": 1,
      "isActive": true,
      "foodCount": 8
    }
  ]
}
```

---

### GET `/api/v1/categories/{id}`
Get a single category by ID.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Category retrieved successfully",
  "data": {
    "id": 1,
    "name": "Pizza",
    "description": "All types of pizza",
    "imageUrl": "https://cdn.example.com/pizza-cat.jpg",
    "displayOrder": 1,
    "isActive": true,
    "foodCount": 8
  }
}
```

---

### POST `/api/v1/categories?restaurantId={id}`
Create a new category.

**Request Body:**
```json
{
  "name": "Beverages",
  "description": "Cold and hot drinks",
  "imageUrl": "https://cdn.example.com/drinks.jpg",
  "displayOrder": 3
}
```

**Validation:**
- `name` — required, 2–100 characters

**Success Response `201 Created`:** Full `CategoryResponse`

---

### PUT `/api/v1/categories/{id}`
Update a category.

**Request Body:** Same as POST  
**Success Response `200 OK`:** Updated `CategoryResponse`

---

### DELETE `/api/v1/categories/{id}`
Delete a category.

**Success Response `204 No Content`**

---

### GET `/api/v1/categories/{id}/foods`
Get all foods belonging to a specific category (paginated).

**Path Param:** `id` — Category ID

**Query Params:**
| Param | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `offset` | Int | No | `0` | Pagination offset |
| `limit` | Int | No | `20` | Page size (max 100) |

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Foods for category retrieved successfully",
  "data": {
    "data": [
      {
        "id": 1,
        "name": "Margherita Pizza",
        "price": 299.0,
        "imageUrl": "https://cdn.example.com/pizza.jpg",
        "categoryName": "Pizza",
        "isAvailable": true,
        "isVegetarian": true,
        "isSpicy": false
      }
    ],
    "pagination": {
      "currentPage": 0,
      "limit": 20,
      "total": 8,
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false
    }
  }
}
```

**Error `404`:** Category not found

---

## 🪑 Tables

> Base URL: `/api/v1/restaurants/{restaurantId}/tables`

### POST `/api/v1/restaurants/{restaurantId}/tables`
Create a new table for a restaurant.

**Request Body:**
```json
{
  "tableNumber": "T-01",
  "capacity": 4,
  "status": "AVAILABLE"
}
```

**Validation:**
- `tableNumber` — required, must be unique per restaurant
- `capacity` — required, must be > 0
- `status` — optional, default `AVAILABLE`

**Table Status Values:** `AVAILABLE`, `OCCUPIED`, `RESERVED`, `CLEANING`, `MAINTENANCE`

**Success Response `201 Created`:**
```json
{
  "success": true,
  "message": "Table created successfully",
  "data": {
    "id": 1,
    "restaurantId": 1,
    "tableNumber": "T-01",
    "capacity": 4,
    "status": "AVAILABLE",
    "currentOrderId": null,
    "createdAt": "2026-03-28T10:00:00",
    "updatedAt": "2026-03-28T10:00:00",
    "version": 0
  }
}
```

---

### GET `/api/v1/restaurants/{restaurantId}/tables`
Get all tables for a restaurant.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Tables retrieved successfully",
  "data": {
    "tables": [ /* list of TableResponse */ ],
    "total": 10
  }
}
```

---

### GET `/api/v1/restaurants/{restaurantId}/tables/{id}`
Get a single table by ID.

**Success Response `200 OK`:** Single `TableResponse`

---

### GET `/api/v1/restaurants/{restaurantId}/tables/available`
Get all available tables. Optionally filter by minimum capacity.

**Query Param:** `?capacity=4` (optional — returns tables with capacity ≥ 4)

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Available tables retrieved successfully",
  "data": [
    {
      "id": 1,
      "tableNumber": "T-01",
      "capacity": 4,
      "status": "AVAILABLE"
    }
  ]
}
```

---

### GET `/api/v1/restaurants/{restaurantId}/tables/occupied`
Get all currently occupied tables.

**Success Response `200 OK`:** List of `TableResponse`

---

### GET `/api/v1/restaurants/{restaurantId}/tables/status/{status}`
Get tables filtered by status.

**Path Param:** `status` — one of `AVAILABLE`, `OCCUPIED`, `RESERVED`, `CLEANING`, `MAINTENANCE`

**Success Response `200 OK`:** List of `TableResponse`

---

### GET `/api/v1/restaurants/{restaurantId}/tables/count/available`
Get count of available tables.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Available tables count retrieved successfully",
  "data": 5
}
```

---

### PUT `/api/v1/restaurants/{restaurantId}/tables/{id}`
Update table details (number, capacity).

**Request Body:** Same as POST  
**Success Response `200 OK`:** Updated `TableResponse`

---

### PATCH `/api/v1/restaurants/{restaurantId}/tables/{id}/status`
Update table status only.

**Query Param:** `?newStatus=CLEANING`

**Success Response `200 OK`:** Updated `TableResponse`

---

### DELETE `/api/v1/restaurants/{restaurantId}/tables/{id}`
Delete a table.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Table removed",
  "data": "Table deleted successfully"
}
```

---

## 📋 Orders

> Base URL: `/api/v1/restaurants/{restaurantId}/orders`

### POST `/api/v1/restaurants/{restaurantId}/orders`
Create a new order for a table.

**Request Body:**
```json
{
  "tableId": 1,
  "orderType": "OFFLINE",
  "items": [
    {
      "foodId": 3,
      "quantity": 2,
      "specialRequests": "Extra cheese please"
    },
    {
      "foodId": 7,
      "quantity": 1,
      "specialRequests": null
    }
  ],
  "notes": "Birthday celebration, bring candles"
}
```

**Validation:**
- `tableId` — required, table must exist and be `AVAILABLE`
- `items` — required, at least 1 item
- `items[].foodId` — required
- `items[].quantity` — required, must be > 0
- `orderType` — optional, `OFFLINE` (dine-in) or `ONLINE` (delivery/takeaway), default `OFFLINE`

**What happens internally:**
1. Validates restaurant exists
2. Validates table is `AVAILABLE`
3. Fetches each food item and validates it exists
4. Creates `OrderItem` for each with price captured at time of order
5. Calculates `totalAmount` from sum of `(unitPrice × quantity)`
6. Sets table status to `OCCUPIED`
7. Generates unique `orderNumber` e.g. `ORD-20260328-0001`

**Order Status Values:** `PENDING`, `IN_PROGRESS`, `COMPLETED`, `DELIVERED`, `CANCELLED`, `HOLD`

**Success Response `201 Created`:**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "restaurantId": 1,
    "tableId": 1,
    "tableNumber": "T-01",
    "orderNumber": "ORD-20260328-0001",
    "status": "PENDING",
    "orderType": "OFFLINE",
    "items": [
      {
        "id": 1,
        "foodId": 3,
        "foodName": "Margherita Pizza",
        "quantity": 2,
        "unitPrice": 299.00,
        "subtotal": 598.00,
        "itemStatus": "PENDING",
        "specialRequests": "Extra cheese please",
        "createdAt": "2026-03-28T20:00:00"
      }
    ],
    "totalAmount": 598.00,
    "notes": "Birthday celebration, bring candles",
    "createdAt": "2026-03-28T20:00:00",
    "updatedAt": "2026-03-28T20:00:00",
    "version": 0
  }
}
```

---

### POST `/api/v1/restaurants/{restaurantId}/orders/{orderId}/items`
Add an item to an existing order.

**Path Params:** `restaurantId`, `orderId`

**Request Body:**
```json
{
  "foodId": 5,
  "quantity": 1,
  "specialRequests": "No onions"
}
```

> ⚠️ Only allowed when order status is `PENDING` or `HOLD`

**Success Response `200 OK`:** Updated full `OrderResponse`

---

### GET `/api/v1/restaurants/{restaurantId}/orders`
Get all orders for a restaurant.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "orders": [ /* list of OrderResponse */ ],
    "total": 25,
    "status": "success"
  }
}
```

---

### GET `/api/v1/restaurants/{restaurantId}/orders/{orderId}`
Get a single order by ID.

**Success Response `200 OK`:** Single `OrderResponse`  
**Error `404`:** Order not found

---

### GET `/api/v1/restaurants/{restaurantId}/orders/status/{status}`
Get orders filtered by status.

**Path Param:** `status` — `PENDING`, `IN_PROGRESS`, `COMPLETED`, `DELIVERED`, `CANCELLED`, `HOLD`

**Success Response `200 OK`:** `OrderListResponse`

---

### GET `/api/v1/restaurants/{restaurantId}/orders/active`
Get all active orders (not `DELIVERED` or `CANCELLED`).

**Success Response `200 OK`:** `OrderListResponse`

---

### GET `/api/v1/restaurants/{restaurantId}/orders/range`
Get orders within a date/time range.

**Query Params:**
- `startDate` — ISO datetime e.g. `2026-03-28T00:00:00`
- `endDate` — ISO datetime e.g. `2026-03-28T23:59:59`

**Success Response `200 OK`:** `OrderListResponse`

---

### GET `/api/v1/restaurants/{restaurantId}/orders/count/pending`
Get count of pending orders.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Pending orders count retrieved",
  "data": {
    "pending_count": 7
  }
}
```

---

### PATCH `/api/v1/restaurants/{restaurantId}/orders/{orderId}/status`
Update order status.

**Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

**Valid Status Transitions:**
- `PENDING` → `IN_PROGRESS`, `HOLD`, `CANCELLED`
- `IN_PROGRESS` → `COMPLETED`, `CANCELLED`
- `COMPLETED` → `DELIVERED`
- `HOLD` → `PENDING`, `CANCELLED`

**Success Response `200 OK`:** Updated `OrderResponse`

---

### PUT `/api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}`
Update an order item's quantity/special requests.

**Request Body:**
```json
{
  "quantity": 3,
  "specialRequests": "Extra spicy"
}
```

**Success Response `200 OK`:** Updated `OrderResponse`

---

### PATCH `/api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}/status`
Update individual item status.

**Query Param:** `?newStatus=PREPARING`

**Item Status Values:** `PENDING`, `PREPARING`, `READY`, `SERVED`, `CANCELLED`

**Success Response `200 OK`:** Updated `OrderResponse`

---

### DELETE `/api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}`
Remove an item from an order.

**Success Response `200 OK`:** Updated `OrderResponse` (without removed item)

---

### DELETE `/api/v1/restaurants/{restaurantId}/orders/{orderId}`
Cancel an order.

**Success Response `200 OK`:** Updated `OrderResponse` with status `CANCELLED`

---

### POST `/api/v1/restaurants/{restaurantId}/orders/{orderId}/generate-bill`
**Auto-generate a bill for an order. No manual tax calculation needed.**

Backend computes:
- `subtotal` — from order items total
- `cgstAmount` — 9% of subtotal
- `sgstAmount` — 9% of subtotal
- `taxAmount` — 18% of subtotal
- `totalAmount` — subtotal + tax - discount
- `billNumber` — auto-generated as `BILL-{restaurantId}-{yyyyMMdd}-{seq}`

**Query Param:** `?discount=50.00` (optional, default `0`)

> ⚠️ Fails with `409 CONFLICT` if a bill already exists for this order.

**Success Response `201 Created`:**
```json
{
  "success": true,
  "message": "Bill generated successfully",
  "data": {
    "id": 1,
    "billNumber": "BILL-1-20260404-0001",
    "orderId": 1,
    "restaurantId": 1,
    "subtotal": 598.00,
    "cgstAmount": 53.82,
    "sgstAmount": 53.82,
    "taxAmount": 107.64,
    "discountAmount": 0.00,
    "totalAmount": 705.64,
    "status": "ISSUED",
    "createdAt": "2026-04-04T20:30:00",
    "updatedAt": "2026-04-04T20:30:00"
  }
}
```

---

### GET `/api/v1/restaurants/{restaurantId}/orders/search`
Search orders by order number, table number, or status.

**Query Param:** `?q=ORD-20260404` (required)

**Example:** `GET /api/v1/restaurants/1/orders/search?q=T-01`

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "orders": [ /* matching OrderResponse list */ ],
    "total": 2,
    "status": "success"
  }
}
```

---

## 🧾 Bills

### POST `/api/v1/bills`
Create a bill manually. **Recommended alternative:** use `POST /orders/{orderId}/generate-bill` which auto-calculates all tax.

**Request Body:**
```json
{
  "billNumber": "BILL-1-20260404-0001",
  "orderId": 1,
  "restaurantId": 1,
  "subtotal": 598.00,
  "taxAmount": 107.64,
  "cgstAmount": 53.82,
  "sgstAmount": 53.82,
  "discountAmount": 0.00,
  "totalAmount": 705.64,
  "status": "ISSUED"
}
```

> `billNumber` is **optional** — if not provided, server auto-generates: `BILL-{restaurantId}-{yyyyMMdd}-{seq}` e.g. `BILL-1-20260404-0001`

**Tax Info:** 18% GST split as 9% CGST + 9% SGST

**Validation:**
- `orderId` — required, order must exist, cannot already have a bill
- `totalAmount` — must be > 0

**Bill Status Values:** `ISSUED`, `PAID`, `CANCELLED`

**Success Response `201 Created`:**
```json
{
  "success": true,
  "message": "Bill created successfully",
  "data": {
    "id": 1,
    "billNumber": "BILL-1-20260404-0001",
    "orderId": 1,
    "restaurantId": 1,
    "restaurantName": "Pizza Palace",
    "subtotal": 598.00,
    "taxAmount": 107.64,
    "cgstAmount": 53.82,
    "sgstAmount": 53.82,
    "discountAmount": 0.00,
    "totalAmount": 705.64,
    "status": "ISSUED",
    "billItems": [],
    "createdAt": "2026-04-04T20:30:00",
    "updatedAt": "2026-04-04T20:30:00"
  }
}
```

---

### GET `/api/v1/bills`
Get all bills (paginated), optionally filtered by status.

**Query Params:**
- `status` — optional: `ISSUED`, `PAID`, `CANCELLED`
- Standard Spring Pageable: `page`, `size`, `sort`

**Success Response `200 OK`:** Spring `Page<BillListResponse>`

---

### GET `/api/v1/bills/{id}`
Get a bill by ID.

**Success Response `200 OK`:** Full `BillResponse`

---

### GET `/api/v1/bills/number/{billNumber}`
Get a bill by its bill number.

**Path Param:** `billNumber` e.g. `BILL-20260328-0001`

**Success Response `200 OK`:** Full `BillResponse`

---

### PUT `/api/v1/bills/{id}`
Update a bill (only when status is `ISSUED`).

**Request Body:** Same as POST  
**Success Response `200 OK`:** Updated `BillResponse`

---

### PATCH `/api/v1/bills/{id}/paid`
Mark a bill as paid.

> ⚠️ Bill must be in `ISSUED` status

**Success Response `200 OK`:** Updated `BillResponse` with status `PAID`

---

### PATCH `/api/v1/bills/{id}/cancel`
Cancel a bill.

> ⚠️ Bill must be in `ISSUED` status

**Success Response `200 OK`:** Updated `BillResponse` with status `CANCELLED`

---

### POST `/api/v1/bills/{id}/items`
Add items to a bill.

**Request Body:**
```json
[
  {
    "foodId": 3,
    "quantity": 2,
    "unitPrice": 299.00
  }
]
```

**Success Response `201 Created`:** Updated `BillResponse`

---

### DELETE `/api/v1/bills/{id}/items/{itemId}`
Remove an item from a bill.

**Success Response `200 OK`:** Updated `BillResponse`

---

### DELETE `/api/v1/bills/{id}`
Delete a bill permanently.

**Success Response `204 No Content`**

---

## 💳 Payments

### POST `/api/v1/payments`
Process a payment for a bill/order.

> **Idempotency:** Use a unique `referenceNumber` per payment. If a payment with the same `referenceNumber` already succeeded, the existing payment is returned — no duplicate charge. `X-Idempotency-Key` header is **not used**.

**Request Body:**
```json
{
  "billId": 1,
  "orderId": 1,
  "paymentMethod": "CARD",
  "amount": 705.64,
  "referenceNumber": "REF-20260328-001",
  "transactionId": "TXN-HDFC-9823741",
  "notes": "Paid via Visa card"
}
```

**Validation:**
- `orderId` — required
- `paymentMethod` — required: `CASH`, `CARD`, `UPI`, `WALLET`
- `amount` — required, must be > 0
- `referenceNumber` — required, unique (used for idempotency)
- `billId` — optional

**What happens internally:**
1. Validates order exists
2. Checks `referenceNumber` for duplicate (if already `SUCCESS`, returns existing payment — idempotent)
3. Validates payment method
4. Validates amount > 0
5. Links to bill if `billId` provided
6. Creates payment with `PENDING` status
7. When status updated to `SUCCESS` → automatically marks linked bill as `PAID`

**Payment Status Values:** `PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "id": 1,
    "billId": 1,
    "orderId": 1,
    "paymentMethod": "CARD",
    "amount": 705.64,
    "status": "PENDING",
    "transactionId": "TXN-HDFC-9823741",
    "referenceNumber": "REF-20260328-001",
    "notes": "Paid via Visa card",
    "createdAt": "2026-03-28T20:35:00",
    "updatedAt": "2026-03-28T20:35:00"
  }
}
```

---

### GET `/api/v1/payments/{id}`
Get a payment by ID.

**Success Response `200 OK`:** Full `PaymentResponse`  
**Error `404`:** Payment not found

---

### GET `/api/v1/payments/bill/{billId}`
Get all payments for a specific bill (paginated).

**Query Params:** `offset` (default 0), `limit` (default 20)

**Success Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "payments": [
      {
        "id": 1,
        "orderId": 1,
        "paymentMethod": "CARD",
        "amount": 705.64,
        "status": "SUCCESS",
        "referenceNumber": "REF-20260328-001",
        "createdAt": "2026-03-28T20:35:00"
      }
    ],
    "meta": {
      "total": 1,
      "limit": 20,
      "offset": 0,
      "has_more": false
    }
  }
}
```

---

### GET `/api/v1/payments/order/{orderId}`
Get all payments for a specific order (paginated).

**Query Params:** `offset` (default 0), `limit` (default 20)

**Success Response `200 OK`:** Same format as `/payments/bill/{billId}`

---

### PATCH `/api/v1/payments/{id}/status`
Update payment status. Use this to transition a payment through its lifecycle.

**Valid transitions:**
- `PENDING` → `SUCCESS` (payment confirmed)
- `PENDING` → `FAILED` (payment failed)
- `SUCCESS` → `REFUNDED` (refund issued)

> When status is set to `SUCCESS`, the linked bill is **automatically marked as `PAID`**.

**Request Body:**
```json
{
  "status": "SUCCESS",
  "transactionId": "TXN-HDFC-9823741",
  "notes": "Confirmed via bank"
}
```

**Validation:**
- `status` — required: `SUCCESS`, `FAILED`, `REFUNDED`
- `transactionId` — optional, updates existing value
- `notes` — optional

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Payment status updated to SUCCESS",
  "data": {
    "id": 1,
    "status": "SUCCESS",
    "billId": 1,
    "orderId": 1,
    "amount": 705.64,
    "paymentMethod": "CARD",
    "referenceNumber": "REF-20260328-001",
    "transactionId": "TXN-HDFC-9823741",
    "createdAt": "2026-04-04T20:35:00",
    "updatedAt": "2026-04-04T20:36:00"
  }
}
```

---

### PATCH `/api/v1/payments/{id}/process`
Shortcut to mark a payment as `SUCCESS` in one call.

> Equivalent to `PATCH /status` with `{ "status": "SUCCESS" }`.  
> Automatically marks the linked bill as `PAID`.  
> Only works when payment is in `PENDING` status.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": { /* full PaymentResponse with status: SUCCESS */ }
}
```

---

### PATCH `/api/v1/payments/{id}/refund`
Mark a payment as `REFUNDED`.

> Only works when payment is in `SUCCESS` status.

**Success Response `200 OK`:**
```json
{
  "success": true,
  "message": "Payment refunded successfully",
  "data": { /* full PaymentResponse with status: REFUNDED */ }
}
```

---

## 🗄️ Data Storage — How It Works

### Database: PostgreSQL
All data is stored in PostgreSQL. Flyway manages schema migrations automatically on startup.

### Tables & Relationships

```
restaurant (1)
  ├── food (N)          — restro_id FK
  ├── categories (N)    — restaurant_id FK
  ├── tables (N)        — restaurant_id FK
  │     └── orders (N)  — table_id FK
  │           ├── order_items (N)   — order_id FK → food_id FK
  │           ├── bills (1)         — order_id FK → restaurant_id FK
  │           │     └── bill_items (N) — bill_id FK → food_id FK
  │           └── payments (N)      — order_id FK → bill_id FK (optional)
  └── users (N)         — restaurant_id FK
```

### Key Data Rules
| Rule | Detail |
|------|--------|
| **Price capture** | Food price is copied to `order_items.unit_price` at order time — price changes don't affect old orders |
| **Duplicate bill prevention** | Only one bill per order allowed — checked before create |
| **Idempotent payments** | Duplicate `referenceNumber` with `SUCCESS` status returns the existing payment, no double charge |
| **Table auto-occupy** | Creating an order automatically sets table status to `OCCUPIED` |
| **Bill auto-pay** | When payment status updated to `SUCCESS`, linked bill status auto-updates to `PAID` |
| **Optimistic locking** | All entities have `version` field — concurrent updates are safely rejected |
| **Soft totals** | Order `totalAmount` is recalculated whenever items are added/removed |
| **GST split** | 18% GST = 9% CGST + 9% SGST — both stored separately in bills table |

### Enum Reference

**OrderStatus**
| Value | Meaning |
|-------|---------|
| `PENDING` | Order placed, not yet started |
| `IN_PROGRESS` | Kitchen is preparing |
| `COMPLETED` | Food ready, not yet delivered |
| `DELIVERED` | Delivered to table |
| `CANCELLED` | Order cancelled |
| `HOLD` | On hold |

**TableStatus**
| Value | Meaning |
|-------|---------|
| `AVAILABLE` | Empty, ready for new order |
| `OCCUPIED` | Has active order |
| `RESERVED` | Pre-booked |
| `CLEANING` | Being cleaned |
| `MAINTENANCE` | Not usable |

**BillStatus**
| Value | Meaning |
|-------|---------|
| `ISSUED` | Bill generated, not yet paid |
| `PAID` | Payment received |
| `CANCELLED` | Bill voided |

**PaymentMethod**
| Value | Meaning |
|-------|---------|
| `CASH` | Cash payment |
| `CARD` | Credit/Debit card |
| `UPI` | UPI (GPay, PhonePe, etc.) |
| `WALLET` | Digital wallet |

**PaymentStatus**
| Value | Meaning |
|-------|---------|
| `PENDING` | Payment initiated |
| `SUCCESS` | Payment confirmed |
| `FAILED` | Payment failed |
| `REFUNDED` | Amount refunded |

---

## 🔄 Typical Mobile App Flow

```
1. Login
   POST /api/v1/auth/login → get JWT token

2. Load restaurant data
   GET /api/v1/categories?restaurantId=1
   GET /api/v1/foods?restaurantId=1

3. Check available tables
   GET /api/v1/restaurants/1/tables/available

4. Customer sits — create order
   POST /api/v1/restaurants/1/orders
     { tableId, orderType: "OFFLINE", items: [{foodId, quantity}] }

5. Kitchen updates
   PATCH /api/v1/restaurants/1/orders/{orderId}/status
     { status: "IN_PROGRESS" }

6. Add more items if needed
   POST /api/v1/restaurants/1/orders/{orderId}/items

7. Generate bill (backend auto-calculates all tax)
   POST /api/v1/restaurants/1/orders/{orderId}/generate-bill
     ?discount=0   ← optional discount amount

8. Collect payment
   POST /api/v1/payments
     { billId, orderId, paymentMethod, amount, referenceNumber }

9. Mark payment as success → bill auto-marked PAID
   PATCH /api/v1/payments/{id}/process
   (or use PATCH /api/v1/payments/{id}/status with { "status": "SUCCESS" })

10. Free the table
    PATCH /api/v1/restaurants/1/tables/{tableId}/status
      ?newStatus=AVAILABLE
```

---

## 🔒 Optimistic Locking (Concurrent Updates)

All `Order` and `Table` entities have a `version` field to handle concurrent updates safely.

### How it works
- Every response includes a `version` field (integer, starts at 0)
- When two clients fetch the same order simultaneously and both try to update it, the **second update will be rejected** with `409 CONFLICT`
- The client should re-fetch the resource and retry with the latest `version`

### Example
```
Client A fetches Order 1 → version: 3
Client B fetches Order 1 → version: 3

Client A updates → SUCCESS, version becomes 4
Client B updates → FAILS with 409 CONFLICT (their version 3 is stale)

Client B re-fetches Order 1 → version: 4
Client B retries update → SUCCESS, version becomes 5
```

### Conflict Error Response `409 CONFLICT`
```json
{
  "success": false,
  "error": {
    "code": "CONFLICT",
    "message": "The resource was modified by another request. Please re-fetch and retry."
  }
}
```

### Mobile team guidance
- Always store the `version` value from responses
- If you receive `409 CONFLICT` on an update, re-fetch the resource and retry
- Do **not** manually increment or send the `version` field — it is managed automatically by the server

---

## 🔁 Payment Idempotency

### How it works
Idempotency is guaranteed through the `referenceNumber` field in the payment request.

- If a payment with the same `referenceNumber` already has status `SUCCESS`, the API returns the **existing payment** — no duplicate charge
- This protects against network retries and double-taps

### Rules
- `referenceNumber` must be **unique per payment attempt**
- Generate it on the mobile side: e.g. `REF-{userId}-{timestamp}` or a UUID
- Do **not** reuse the same `referenceNumber` for different payments

### ❌ X-Idempotency-Key Header
The `X-Idempotency-Key` request header is **not used**. It appears in older documentation — ignore it. Idempotency is handled **only via `referenceNumber`** in the request body.

---

## ⚠️ Notes for Mobile Team

1. **All endpoints** (including Bills) return the standard `{ success, data, message, error }` `ApiResponse` envelope.

2. **Payment lifecycle** — Payment is created as `PENDING`. Use one of these to confirm:
   - `PATCH /api/v1/payments/{id}/process` — shortcut to mark as `SUCCESS`
   - `PATCH /api/v1/payments/{id}/status` with body `{ "status": "SUCCESS" }` — full control

3. **`GET /api/v1/foods`** requires `restaurantId` to return results. Use `GET /api/v1/foods/search` for global search without restaurantId.

4. **Table status** is managed manually — after payment the app should call `PATCH /tables/{id}/status?newStatus=AVAILABLE` to free the table.

5. **Bill generation** — Use `POST /orders/{orderId}/generate-bill` to auto-generate a bill. Backend handles all tax calculations (18% GST = 9% CGST + 9% SGST). Manual `POST /api/v1/bills` still available if needed.

6. **Bill number** — `billNumber` is optional in `POST /api/v1/bills`. If not provided, server auto-generates: `BILL-{restaurantId}-{yyyyMMdd}-{seq}`.

7. **Date format** — All timestamps are ISO 8601: `2026-03-28T20:00:00` (server local time, IST).

8. **Order type** — Send `orderType: "OFFLINE"` for dine-in, `orderType: "ONLINE"` for delivery/takeaway. Defaults to `OFFLINE` if not provided.


