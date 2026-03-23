# 🔧 ENHANCEMENT RECOMMENDATIONS

**Date:** March 23, 2026  
**Purpose:** Action items to implement based on cross-check analysis  
**Priority:** Phase 0 & Phase 1

---

## 🎯 PRIORITY 1: Phase 0 (CRITICAL)

### 1.1 Add Check Constraints to Migrations

**File:** `V8__Enhance_food_table.sql`

Add at the end:
```sql
ALTER TABLE food 
  ADD CONSTRAINT check_food_price CHECK (price > 0);

ALTER TABLE food 
  ADD CONSTRAINT check_prep_time CHECK (preparation_time > 0 OR preparation_time IS NULL);

ALTER TABLE food 
  ADD CONSTRAINT check_calories CHECK (calories > 0 OR calories IS NULL);
```

**File:** `V10__Create_orders_table.sql`

Add:
```sql
ALTER TABLE orders 
  ADD CONSTRAINT check_subtotal CHECK (subtotal >= 0);

ALTER TABLE orders 
  ADD CONSTRAINT check_discount CHECK (discount_amount >= 0);

ALTER TABLE orders 
  ADD CONSTRAINT check_tax CHECK (tax_amount >= 0);

ALTER TABLE orders 
  ADD CONSTRAINT check_total CHECK (total_amount >= 0);
```

**File:** `V11__Create_order_items_table.sql`

Add:
```sql
ALTER TABLE order_items 
  ADD CONSTRAINT check_quantity CHECK (quantity > 0);

ALTER TABLE order_items 
  ADD CONSTRAINT check_unit_price CHECK (unit_price >= 0);

ALTER TABLE order_items 
  ADD CONSTRAINT check_subtotal CHECK (subtotal >= 0);
```

**File:** `V12__Create_bills_table.sql`

Add:
```sql
ALTER TABLE bills 
  ADD CONSTRAINT check_bill_total CHECK (total_amount >= 0);

ALTER TABLE bills 
  ADD CONSTRAINT check_discount_value CHECK (discount_value > 0 OR discount_value IS NULL);

ALTER TABLE bills 
  ADD CONSTRAINT check_cgst_rate CHECK (cgst_rate > 0);

ALTER TABLE bills 
  ADD CONSTRAINT check_sgst_rate CHECK (sgst_rate > 0);
```

**File:** `V14__Create_payments_table.sql`

Add:
```sql
ALTER TABLE payments 
  ADD CONSTRAINT check_amount CHECK (amount > 0);
```

**File:** `V7__Create_tables_table.sql`

Add:
```sql
ALTER TABLE tables 
  ADD CONSTRAINT check_capacity CHECK (capacity > 0);

ALTER TABLE tables 
  ADD CONSTRAINT check_floor CHECK (floor >= 0);
```

---

### 1.2 Create ApiResponse Wrapper

**File:** `src/main/kotlin/com/autobill/billsmart/dto/ApiResponse.kt`

```kotlin
package com.autobill.billsmart.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetails? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message)

        fun <T> error(
            code: String,
            message: String,
            details: Map<String, Any>? = null
        ): ApiResponse<T> =
            ApiResponse(
                success = false,
                error = ErrorDetails(code = code, message = message, details = details)
            )
    }
}

data class ErrorDetails(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)

// Pagination wrapper
data class PaginatedResponse<T>(
    val data: List<T>,
    val current_page: Int,
    val limit: Int,
    val total: Int,
    val has_more: Boolean
)
```

---

### 1.3 Add @Transactional to Services

**File:** `src/main/kotlin/com/autobill/billsmart/services/impl/FoodServiceImpl.kt`

```kotlin
import org.springframework.transaction.annotation.Transactional

@Service
class FoodServiceImpl(
    // ... existing dependencies
) : FoodService {

    @Transactional
    override fun createFood(restroId: Long, req: FoodRequest): FoodResponse {
        val restaurant = restaurantRepositoryPort.findById(restroId)
            ?: throw ResourceNotFoundException("Restaurant not found: $restroId")
        val food = foodMapper.toFood(req)
        food.restaurant = restaurant
        val saved = foodRepositoryPort.save(food)
        return foodMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    override fun getAllFoods(restroId: Long): List<FoodResponse> {
        restaurantRepositoryPort.findById(restroId)
            ?: throw ResourceNotFoundException("Restaurant not found: $restroId")
        val foods = foodRepositoryPort.findByRestaurantRestroId(restroId)
        return foods.map { foodMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun getFood(id: Long): FoodResponse? =
        foodRepositoryPort.findById(id)?.let { foodMapper.toResponse(it) }
}
```

Do the same for `RestaurantServiceImpl` and `StaffServiceImpl`.

---

### 1.4 Add Exception Handling to Adapters

**File:** `src/main/kotlin/com/autobill/billsmart/adapters/persistence/FoodRepositoryAdapter.kt`

```kotlin
import org.springframework.dao.DataAccessException
import com.autobill.billsmart.exception.PersistenceException

@Repository
class FoodRepositoryAdapter(
    private val foodRepository: FoodRepository
) : FoodRepositoryPort {

    private val log = LoggerFactory.getLogger(FoodRepositoryAdapter::class.java)

    override fun save(food: Food): Food {
        return try {
            val saved = foodRepository.save(food)
            log.debug("Saved food id={}", saved.id)
            saved
        } catch (ex: DataAccessException) {
            log.error("Database error saving food", ex)
            throw PersistenceException("Failed to save food", ex)
        }
    }

    override fun findById(id: Long): Food? {
        return try {
            val found = foodRepository.findById(id).orElse(null)
            if (found == null) {
                log.debug("Food not found for id={}", id)
            } else {
                log.debug("Found food id={}", found.id)
            }
            found
        } catch (ex: DataAccessException) {
            log.error("Database error finding food id={}", id, ex)
            throw PersistenceException("Failed to find food", ex)
        }
    }

    override fun findByRestaurantRestroId(restroId: Long): List<Food> {
        return try {
            val foods = foodRepository.findByRestaurantRestroId(restroId)
            log.debug("Found {} foods for restaurant id={}", foods.size, restroId)
            foods
        } catch (ex: DataAccessException) {
            log.error("Database error finding foods for restaurant id={}", restroId, ex)
            throw PersistenceException("Failed to find foods", ex)
        }
    }
}
```

---

### 1.5 Create Custom Exceptions

**File:** `src/main/kotlin/com/autobill/billsmart/exception/AppException.kt`

```kotlin
package com.autobill.billsmart.exception

sealed class AppException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause) {
    
    data class ValidationException(val message: String) : AppException(message)
    
    data class ResourceNotFoundException(val message: String) : AppException(message)
    
    data class UnauthorizedException(val message: String) : AppException(message)
    
    data class ForbiddenException(val message: String) : AppException(message)
    
    data class ConflictException(val message: String) : AppException(message)
    
    data class PersistenceException(
        val message: String, 
        val cause: Throwable? = null
    ) : AppException(message, cause)
    
    data class InternalServerException(
        val message: String, 
        val cause: Throwable? = null
    ) : AppException(message, cause)
}
```

---

### 1.6 Update Global Exception Handler

**File:** `src/main/kotlin/com/autobill/billsmart/controller/GlobalExceptionHandler.kt`

```kotlin
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): 
        ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "RESOURCE_NOT_FOUND",
                message = ex.message ?: "Resource not found"
            ),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): 
        ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "VALIDATION_ERROR",
                message = ex.message ?: "Validation failed"
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): 
        ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "CONFLICT",
                message = ex.message ?: "Conflict occurred"
            ),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(PersistenceException::class)
    fun handlePersistence(ex: PersistenceException): 
        ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "DATABASE_ERROR",
                message = "Database operation failed"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): 
        ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity(
            ApiResponse.error<Nothing>(
                code = "INTERNAL_SERVER_ERROR",
                message = "An unexpected error occurred"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
```

---

## 🎯 PRIORITY 2: Phase 1 (HIGH)

### 2.1 Create Remaining Repository Ports

**File:** `src/main/kotlin/com/autobill/billsmart/ports/OrderRepositoryPort.kt`

```kotlin
package com.autobill.billsmart.ports

import com.autobill.billsmart.model.Order

interface OrderRepositoryPort {
    fun save(order: Order): Order
    fun findById(id: Long): Order?
    fun findByOrderNumber(orderNumber: String): Order?
    fun findByRestaurantIdAndStatus(
        restaurantId: Long,
        status: String
    ): List<Order>
}
```

Create similar files for:
- `BillRepositoryPort`
- `PaymentRepositoryPort`
- `CategoryRepositoryPort`
- `TableRepositoryPort`

---

### 2.2 Add Pagination Query Methods

**File:** `src/main/kotlin/com/autobill/billsmart/repositories/FoodRepository.kt`

```kotlin
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FoodRepository : JpaRepository<Food, Long> {
    
    fun findByRestaurantRestroId(restroId: Long): List<Food>
    
    // NEW: Pagination support
    fun findByRestroIdAndIsAvailable(
        restroId: Long,
        isAvailable: Boolean,
        pageable: Pageable
    ): Page<Food>
    
    fun findByRestroIdAndCategoryIdAndIsAvailable(
        restroId: Long,
        categoryId: Long?,
        isAvailable: Boolean,
        pageable: Pageable
    ): Page<Food>
    
    fun findByNameContainingIgnoreCaseAndRestroId(
        name: String,
        restroId: Long,
        pageable: Pageable
    ): Page<Food>
    
    fun findByRestroIdOrderByPrice(
        restroId: Long,
        pageable: Pageable
    ): Page<Food>
}
```

---

### 2.3 Update Service for Pagination

**File:** `src/main/kotlin/com/autobill/billsmart/services/impl/FoodServiceImpl.kt`

```kotlin
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Service
class FoodServiceImpl(
    // ... existing
) : FoodService {

    @Transactional(readOnly = true)
    fun getFoodsPaginated(
        restroId: Long,
        offset: Int = 0,
        limit: Int = 20,
        category: Long? = null,
        sort: String? = null,
        search: String? = null
    ): PaginatedResponse<FoodResponse> {
        restaurantRepositoryPort.findById(restroId)
            ?: throw ResourceNotFoundException("Restaurant not found")

        // Validate limit
        val pageSize = if (limit > 100) 100 else limit
        val pageNum = offset / pageSize

        // Create Pageable with sorting
        val direction = if (sort?.contains("desc") == true) 
            Sort.Direction.DESC else Sort.Direction.ASC
        val field = when {
            sort?.startsWith("price") == true -> "price"
            sort?.startsWith("name") == true -> "name"
            else -> "id"
        }
        val pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, field))

        // Execute query
        val page = if (search != null) {
            foodRepositoryPort.findByNameContainingAndRestaurant(
                search, restroId, pageable
            )
        } else if (category != null) {
            foodRepositoryPort.findByRestaurantAndCategory(
                restroId, category, pageable
            )
        } else {
            foodRepositoryPort.findByRestaurant(restroId, pageable)
        }

        return PaginatedResponse(
            data = page.content.map { foodMapper.toResponse(it) },
            current_page = pageNum,
            limit = pageSize,
            total = page.totalElements.toInt(),
            has_more = page.hasNext()
        )
    }
}
```

---

### 2.4 Update Controllers with Response Wrapper

**File:** `src/main/kotlin/com/autobill/billsmart/controllers/FoodController.kt`

```kotlin
import com.autobill.billsmart.dto.ApiResponse

@RestController
@RequestMapping("/api/v1/foods")
class FoodController(
    private val foodService: FoodService
) {

    @PostMapping
    fun createFood(
        @Valid @RequestBody req: FoodRequest
    ): ResponseEntity<ApiResponse<FoodResponse>> {
        val saved = foodService.createFood(req)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(saved, "Food created successfully"))
    }

    @GetMapping
    fun getFoods(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) category: Long?,
        @RequestParam(required = false) sort: String?
    ): ResponseEntity<ApiResponse<PaginatedResponse<FoodResponse>>> {
        val foods = foodService.getFoodsPaginated(offset, limit, category, sort)
        return ResponseEntity.ok(ApiResponse.success(foods))
    }

    @GetMapping("/{id}")
    fun getFood(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<FoodResponse>> {
        val food = foodService.getFood(id)
            ?: throw ResourceNotFoundException("Food not found")
        return ResponseEntity.ok(ApiResponse.success(food))
    }
}
```

---

## 📝 Implementation Checklist

### Phase 0 - Complete Before Starting Phase 1
- [ ] Add all check constraints to migrations
- [ ] Create ApiResponse wrapper
- [ ] Add @Transactional to all services
- [ ] Add exception handling to adapters
- [ ] Create custom exceptions
- [ ] Update GlobalExceptionHandler

### Phase 1 - Complete During Phase 1
- [ ] Create remaining repository ports
- [ ] Add pagination query methods
- [ ] Update services for pagination
- [ ] Update controllers with response wrapper
- [ ] Add comprehensive validation
- [ ] Test all endpoints

---

## ✅ Verification Before Phase 1

Run this checklist:
```
Migrations:
  [ ] All 15 migrations execute successfully
  [ ] All constraints in place
  [ ] Indexes created
  
Code:
  [ ] No compilation errors
  [ ] All @Transactional annotations in place
  [ ] Exception handling in all adapters
  [ ] ApiResponse wrapper working
  
Testing:
  [ ] Unit tests pass
  [ ] Integration tests pass
  [ ] Postman collection updated
  [ ] Error responses in correct format
```

---

**All recommendations are implementable in Phase 0-1 without breaking changes. They enhance robustness and production-readiness.**

