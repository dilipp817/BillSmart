# 🎯 Clean Commit Strategy for BillSmart Implementation

**Purpose:** Guide for making organized, clean Git commits throughout the project  
**Benefit:** Clear project history, easy bisecting, professional codebase

---

## Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style (formatting, semicolons, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvement
- `test`: Adding or updating tests
- `chore`: Build, dependencies, tools
- `ci`: CI/CD configuration

### Examples
```
feat(auth): Add JWT token generation and validation
feat(food): Implement Foods API with pagination and filtering
feat(database): Create categories table with indexes
fix(order): Prevent duplicate order creation
test(bill): Add tax calculation unit tests
docs(api): Add endpoint documentation for Phase 1
chore(deps): Add io.jsonwebtoken dependency
```

---

## Phase 0: Database Commits

### Commit 1: User Entity Enhancement
```
feat(database): Enhance users table with auth fields

- Add password column for authentication
- Add role column for authorization
- Add restaurant_id for multi-tenancy
- Add is_active for soft delete support
- Add device tracking columns
- Add last_login for audit trail
- Create indexes on critical fields

Migration: V5__Enhance_users_table.sql
```

### Commit 2: Create Categories Table
```
feat(database): Create categories table

- Add categories table for menu organization
- Create unique constraint per restaurant
- Add display_order for custom ordering
- Create performance indexes

Migration: V6__Create_categories_table.sql
```

### Commit 3: Create Tables Table
```
feat(database): Create tables table

- Add restaurant seating management
- Track table status and current orders
- Create indexes for queries

Migration: V7__Create_tables_table.sql
```

### Commit 4: Enhance Food Table
```
feat(database): Enhance food table with extended fields

- Add category_id for categorization
- Add image_url for media
- Add availability tracking
- Add metadata (prep time, allergens, calories)
- Create performance indexes

Migration: V8__Enhance_food_table.sql
```

### Commit 5: Enhance Restaurant Table
```
feat(database): Enhance restaurant table with tax config

- Add tax_rate for GST calculation
- Add GSTIN and PAN for India compliance
- Add is_active for deactivation

Migration: V9__Enhance_restaurant_table.sql
```

### Commits 6-11: Orders, Bills, Payments
```
feat(database): Create orders table
feat(database): Create order_items table
feat(database): Create bills table
feat(database): Create bill_items table
feat(database): Create payments table
feat(database): Create all performance indexes

Migrations: V10-V15
```

---

## Phase 1: Authentication & Foods

### Commit 1: Auth Infrastructure
```
feat(auth): Add JWT authentication infrastructure

- Add JWT token provider service
- Add security configuration
- Add password encoder utility
- Add JWT filter for request validation

Files:
- config/JwtConfig.kt
- config/SecurityConfig.kt
- util/TokenProvider.kt
- filter/JwtFilter.kt
- util/PasswordEncoder.kt

Dependencies added:
- io.jsonwebtoken:jjwt
```

### Commit 2: Auth DTOs & Mappers
```
feat(auth): Add authentication DTOs

- Add LoginRequest DTO
- Add LoginResponse DTO
- Add TokenResponse DTO
- Add UserResponse DTO
- Add AuthMapper

Files:
- dto/auth/LoginRequest.kt
- dto/auth/LoginResponse.kt
- dto/auth/TokenResponse.kt
- dto/auth/UserResponse.kt
- mapper/AuthMapper.kt
```

### Commit 3: Auth Service
```
feat(auth): Implement authentication service

- Add AuthService interface
- Add AuthServiceImpl with login logic
- Add password verification
- Add token generation
- Add device tracking
- Add error handling

Files:
- service/auth/AuthService.kt
- service/auth/AuthServiceImpl.kt
- repository/UserRepository.kt (new methods)
```

### Commit 4: Auth Controller & Endpoint
```
feat(auth): Add authentication REST endpoint

- Add AuthController
- Implement POST /api/v1/auth/login
- Add response wrapper
- Add error handling
- Add validation

Files:
- controller/AuthController.kt
- dto/ApiResponse.kt
- exception/AppException.kt
- controller/GlobalExceptionHandler.kt
```

### Commit 5: Foods API - Service Layer
```
feat(food): Implement Foods API service with pagination

- Add FoodService interface
- Add FoodServiceImpl with filtering logic
- Implement pagination calculation
- Implement sorting (price, name)
- Implement search functionality
- Add category filtering

Files:
- service/food/FoodService.kt
- service/food/FoodServiceImpl.kt
- repository/FoodRepository.kt (new queries)
- validator/FoodQueryValidator.kt
```

### Commit 6: Foods API - DTOs & Mapper
```
feat(food): Add Foods API DTOs

- Add FoodResponse DTO with all fields
- Add PaginatedResponse<T> wrapper
- Add FoodFilterRequest DTO
- Add FoodMapper for conversions

Files:
- dto/food/FoodResponse.kt
- dto/food/PaginatedResponse.kt
- dto/food/FoodFilterRequest.kt
- mapper/FoodMapper.kt
```

### Commit 7: Foods API - Controller & Endpoint
```
feat(food): Add Foods REST endpoint

- Add FoodController
- Implement GET /api/v1/foods
- Add query parameter validation
- Add pagination metadata in response
- Add error handling

Files:
- controller/FoodController.kt
```

### Commit 8: Categories API
```
feat(category): Implement Categories API

- Add Category entity
- Add CategoryService with item counting
- Add CategoryController
- Implement GET /api/v1/categories
- Add ordering by display_order

Files:
- model/Category.kt
- service/category/CategoryService.kt
- service/category/CategoryServiceImpl.kt
- controller/CategoryController.kt
- repository/CategoryRepository.kt
- dto/category/CategoryResponse.kt
- mapper/CategoryMapper.kt
```

### Commit 9: Error Handling & Response Wrapper
```
feat(core): Add global error handling and response wrapper

- Add ApiResponse wrapper class
- Add ErrorDetails class
- Add GlobalExceptionHandler with @RestControllerAdvice
- Map all exceptions to proper status codes
- Add validation error responses

Files:
- dto/ApiResponse.kt
- dto/ErrorDetails.kt
- exception/AppException.kt
- exception/ErrorCode.kt
- controller/GlobalExceptionHandler.kt
```

### Commit 10: Configuration & Properties
```
chore(config): Add JWT configuration properties

- Add JWT secret to properties
- Add token expiration settings
- Add environment-specific configs
- Add database and CORS settings

Files (modified):
- application.properties
- application-dev.properties
- application-prod.properties
```

### Commit 11: Test Data & Migration
```
test(auth): Add test users for authentication

- Create migration with hashed test user
- Add default restaurant and food items
- Add test categories

Migration: V16__Insert_test_data.sql
```

### Commit 12: Test Implementation
```
test(api): Add comprehensive API tests

- Add AuthControllerTest
- Add FoodControllerTest
- Add CategoryControllerTest
- Add pagination tests
- Add filtering tests
- Add error case tests

Files:
- controller/AuthControllerTest.kt
- controller/FoodControllerTest.kt
- controller/CategoryControllerTest.kt
```

### Commit 13: Postman Collection
```
docs(api): Add Postman collection for Phase 1

- Add 3 endpoint examples
- Add authentication setup
- Add example requests and responses
- Add environment variables

Files:
- postman_collection_phase1.json
```

---

## Phase 2: Orders (Similar Structure)

```
feat(order): Create Order entity and relationships
feat(order): Implement order service with calculations
feat(order): Add Orders API controller
feat(order): Add order validation and state machine
test(order): Add comprehensive order tests
docs(api): Update Postman collection with order endpoints
```

---

## Commit Best Practices

### DO ✅
```
✅ Make small, focused commits
✅ Write descriptive commit messages
✅ One feature per commit
✅ Include issue/ticket numbers if available
✅ Test before committing
✅ Commit frequently (multiple per day)
✅ Reference related commits
```

### DON'T ❌
```
❌ Mix multiple features in one commit
❌ Write vague messages ("fix stuff", "updates")
❌ Commit broken code
❌ Commit temporary debug code
❌ Make huge commits with hundreds of changes
❌ Commit without testing locally first
❌ Force push to shared branches
```

---

## Example Commit with Body

```
feat(order): Implement order creation with tax calculations

Implement the POST /api/v1/orders endpoint with complete
business logic including:
- Table availability validation
- Order item validation
- Subtotal calculation
- Tax calculation (18% GST = 9% CGST + 9% SGST)
- Order status initialization
- Table status update to 'occupied'

The implementation uses atomic transactions to ensure
data consistency. Order numbers are generated in format
ORD-YYYY-MM-DD-XXX with daily sequence counter.

Fixes #42
Related to #41, #43

Co-authored-by: Teammate <email@example.com>
```

---

## Branch Strategy

### Main Branches
```
main         - Production code (stable, tested)
develop      - Development integration branch
             
staging      - Pre-production testing
```

### Feature Branches
```
feature/auth-setup
feature/foods-api
feature/orders-implementation
feature/billing-system
feature/payments-processing
feature/search-functionality
```

### Commit Flow
```
1. Create feature branch: git checkout -b feature/auth-setup
2. Make small commits with clear messages
3. Create pull request to develop
4. Code review and testing
5. Merge to develop
6. Later: merge develop → main for release
```

---

## Git Workflow Template

```bash
# 1. Create feature branch
git checkout -b feature/orders-api

# 2. Make changes
# ... edit files ...

# 3. Stage changes
git add src/main/kotlin/com/autobill/billsmart/model/Order.kt

# 4. Commit with clear message
git commit -m "feat(order): Create Order entity with relationships"

# 5. Repeat steps 2-4 for each logical change

# 6. Push to remote
git push origin feature/orders-api

# 7. Create Pull Request on GitHub/GitLab

# 8. After review and approval
git checkout develop
git pull origin develop
git merge feature/orders-api
git push origin develop

# 9. Delete feature branch
git branch -d feature/orders-api
git push origin --delete feature/orders-api
```

---

## Commit Frequency Target

### Phase 0 (Database)
- Expected: 15 commits (1 per migration + 1 validation)
- Frequency: Every 1-2 hours during day 1-2

### Phase 1 (Auth & Foods)
- Expected: 13 commits
- Frequency: 2-3 per day over 4-6 days

### Phase 2-5
- Expected: 40+ commits total
- Frequency: 2-4 per day

**Total Expected:** 80-100 commits over 3-4 weeks

---

## Benefits of Clean Commits

✅ **Easy Code Review**
- Reviewer can understand each change
- Tests align with each commit
- Easy to ask for revisions

✅ **Better Debugging**
- Use `git bisect` to find bug origin
- `git blame` shows relevant context
- Revert specific features if needed

✅ **Cleaner History**
- `git log` tells a story
- `git log --oneline` is readable
- Future developers understand evolution

✅ **CI/CD Integration**
- Build and test per commit
- Identify exact commit that broke tests
- Automatic deployment per commit

---

## Template for New Files

### Service Implementation
```kotlin
// commit message: feat(module): Implement [Feature] service
```

### Controller Implementation
```kotlin
// commit message: feat(module): Add [Feature] REST endpoint
```

### Test Implementation
```kotlin
// commit message: test(module): Add [Feature] tests
```

### Database Migration
```sql
-- commit message: feat(database): Create [Table] table
```

---

**Follow these patterns and your project history will be clean, professional, and helpful for the team! 🎉**

