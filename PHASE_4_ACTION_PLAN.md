# 🎯 PHASE 4: Payments & Tables - Implementation Plan

**Status:** 🟢 IN PROGRESS | **Date:** March 29, 2026  
**Duration:** 4-5 Days | **Effort:** 12-16 Hours | **Story Points:** 18-22

---

## 📋 PHASE 4 OVERVIEW

### Goals
1. ✅ Implement Payment processing system
2. ✅ Complete Table management
3. ✅ Add idempotency for payments
4. ✅ Deliver 2 new API endpoints

### Deliverables
- 2 new API endpoints (8 → 10 total)
- Payment service with multiple payment methods
- Idempotency handling
- Table status management
- Updated Postman collection
- Complete integration tests

### Current Status
- ✅ Phase 0: Database (15 migrations)
- ✅ Phase 1: Auth & Foods APIs
- ✅ Phase 2: Orders API
- ✅ Phase 3: Bills & Payments entities
- 🟢 Phase 4: Payment processing & Tables

---

## 🎯 PHASE 4 COMPONENTS

### 1️⃣ Payment Processing (Payments API)
**Endpoint:** `POST /api/v1/payments`

**Features:**
- Multiple payment methods (CASH, CARD, UPI, WALLET)
- Transaction ID generation (TXN-YYYY-MM-DD-XXX)
- Idempotent payments (X-Idempotency-Key header)
- Partial payment support
- Bill status update (UNPAID → PAID)
- Table status clear (OCCUPIED → AVAILABLE)
- Error handling for insufficient amount

**Request:**
```json
{
  "bill_id": 1,
  "order_id": 1,
  "amount": 1250.50,
  "payment_method": "CARD",
  "transaction_id": "TXN-2026-03-29-001",
  "reference_number": "REF-123456",
  "notes": "Payment received"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "payment_id": 1,
    "bill_id": 1,
    "transaction_id": "TXN-2026-03-29-001",
    "amount": 1250.50,
    "payment_method": "CARD",
    "status": "COMPLETED",
    "created_at": "2026-03-29T10:30:00Z"
  }
}
```

### 2️⃣ Table Management (Tables API)
**Endpoint:** `GET /api/v1/tables`

**Features:**
- List all tables for a restaurant
- Filter by status (AVAILABLE, OCCUPIED, RESERVED, OUT_OF_SERVICE)
- Sort by table number
- Table capacity info
- Current order info
- Last used timestamp

**Query Parameters:**
```
GET /api/v1/tables?restaurant_id=1&status=AVAILABLE&offset=0&limit=20
```

**Response:**
```json
{
  "success": true,
  "data": {
    "tables": [
      {
        "table_id": 1,
        "table_number": "T1",
        "capacity": 4,
        "status": "AVAILABLE",
        "current_order_id": null,
        "last_used": "2026-03-29T10:15:00Z"
      }
    ],
    "meta": {
      "total": 20,
      "limit": 20,
      "offset": 0,
      "has_more": false
    }
  }
}
```

---

## 🛠️ IMPLEMENTATION CHECKLIST

### Step 1: Payment Service & Logic
- [ ] Create `PaymentService` interface
- [ ] Create `PaymentServiceImpl` with payment processing
- [ ] Implement transaction ID generation
- [ ] Implement idempotency key handling
- [ ] Add payment status tracking
- [ ] Add error handling

### Step 2: Idempotency Service
- [ ] Create `IdempotencyService` interface
- [ ] Create `IdempotencyServiceImpl`
- [ ] Track idempotency keys
- [ ] Cache responses for duplicate requests
- [ ] Add cleanup for old entries

### Step 3: Payment Controller
- [ ] Create `PaymentController`
- [ ] Implement POST `/api/v1/payments`
- [ ] Add idempotency header support
- [ ] Add validation
- [ ] Add error handling

### Step 4: Table Controller
- [ ] Create `TableController`
- [ ] Implement GET `/api/v1/tables`
- [ ] Add filtering by status
- [ ] Add pagination
- [ ] Add sorting

### Step 5: Integration & Testing
- [ ] Test payment processing
- [ ] Test idempotency
- [ ] Test all payment methods
- [ ] Test error scenarios
- [ ] Test table listing
- [ ] Update Postman collection

---

## 📂 FILES TO CREATE/MODIFY

### New Files
```
src/main/kotlin/com/autobill/billsmart/
├── services/
│   ├── PaymentService.kt (interface)
│   ├── impl/PaymentServiceImpl.kt
│   ├── IdempotencyService.kt (interface)
│   └── impl/IdempotencyServiceImpl.kt
├── controller/
│   ├── PaymentController.kt
│   └── TableController.kt (existing, enhance)
└── util/
    └── TransactionIdGenerator.kt
```

### Modified Files
- `BillRepository.kt` - Add methods for payment tracking
- `TableRepository.kt` - Add methods for table status
- `application.properties` - Add payment config

---

## 🔄 WORKFLOW

### Day 1: Payment Service & Idempotency
1. Create PaymentService interface & implementation
2. Create IdempotencyService interface & implementation
3. Implement transaction ID generation
4. Add unit tests

### Day 2: Controllers & Integration
1. Create PaymentController
2. Create/enhance TableController
3. Add validation & error handling
4. Add integration tests

### Day 3: Testing & Documentation
1. Test all endpoints in Postman
2. Test idempotency with duplicate requests
3. Test error scenarios
4. Update Postman collection
5. Create API documentation

### Day 4-5: Code Review & Optimization
1. Code review & refactoring
2. Performance optimization
3. Final testing
4. Ready for Phase 5

---

## 📊 METRICS

### Code
- New source files: 7-8
- New migration files: 0 (using existing tables)
- New endpoints: 2
- Lines of code: ~400-500

### API Endpoints (Total: 10)
1. ✅ POST /api/v1/auth/login (Phase 1)
2. ✅ GET /api/v1/foods (Phase 1)
3. ✅ GET /api/v1/categories (Phase 1)
4. ✅ POST /api/v1/orders (Phase 2)
5. ✅ GET /api/v1/orders/{id} (Phase 2)
6. ✅ PATCH /api/v1/orders/{id} (Phase 2)
7. ✅ POST /api/v1/bills (Phase 3)
8. ✅ GET /api/v1/bills/{id} (Phase 3)
9. 🟢 **POST /api/v1/payments** (Phase 4)
10. 🟢 **GET /api/v1/tables** (Phase 4)

---

## ✅ SUCCESS CRITERIA

- [ ] All 10 API endpoints working
- [ ] Payments can be processed
- [ ] Idempotency prevents duplicate payments
- [ ] Bill status updated after payment
- [ ] Table status cleared after payment
- [ ] All payment methods supported
- [ ] Error handling working correctly
- [ ] All endpoints tested in Postman
- [ ] 100% test coverage for payment logic
- [ ] Performance metrics acceptable

---

## 🎯 NEXT PHASE (Phase 5)

After Phase 4:
- Additional search endpoints
- Report generation
- Analytics dashboard
- Final optimizations

---

**Ready to start? Let's go! 🚀**

