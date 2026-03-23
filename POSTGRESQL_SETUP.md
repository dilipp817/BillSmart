# 🗄️ PostgreSQL Development Setup Guide

**Date:** March 24, 2026  
**Status:** Updated for Local PostgreSQL Development

---

## ✅ YOUR SETUP (Corrected)

### **Tests Environment**
```
Framework: ./gradlew test
Database: H2 In-Memory
Why: Fast, isolated, no setup needed
Config: application-test.properties
```

### **Development Environment**
```
Framework: ./gradlew bootRun
Database: PostgreSQL (your local setup)
Why: Real database, persistent data, matches production
Config: application-dev.properties
```

### **Production Environment**
```
Framework: Deployed server
Database: PostgreSQL (production server)
Why: ACID compliance, durability, security
Config: application-prod.properties
```

---

## 🔧 SETUP STEPS

### Step 1: Create Development Database

**Option A: Using the script**
```bash
chmod +x setup-postgresql.sh
./setup-postgresql.sh
```

**Option B: Manual SQL**
```sql
CREATE DATABASE billsmart_dev
    WITH 
    ENCODING = 'UTF8'
    LOCALE = 'en_US.UTF-8'
    TEMPLATE = template0;
```

### Step 2: Verify Connection

```bash
psql -U postgres -h localhost -d billsmart_dev
```

You should see:
```
billsmart_dev=# 
```

Exit with: `\q`

### Step 3: Run Application

```bash
./gradlew bootRun
```

**What happens:**
1. Spring Boot starts
2. Loads `application-dev.properties`
3. Connects to `billsmart_dev` PostgreSQL database
4. Flyway runs migrations (V1-V15)
5. All 10 tables created
6. Application ready on port 8443

---

## 📊 CONFIGURATION COMPARISON

### Before (Wrong)
```
Development: H2 (doesn't match production)
Tests: H2 (correct)
Production: PostgreSQL (different!)
❌ Problem: Different databases = different behavior
```

### After (Correct)
```
Development: PostgreSQL (matches production)
Tests: H2 (fast, isolated)
Production: PostgreSQL (same as dev)
✅ Solution: Dev matches prod, tests stay isolated
```

---

## ✅ WHAT'S BEEN UPDATED

| File | Change | Status |
|------|--------|--------|
| `application-dev.properties` | Enabled Flyway, PostgreSQL | ✅ Updated |
| `application-test.properties` | Kept H2 for tests | ✅ Correct |
| Database name | Changed to `billsmart_dev` | ✅ Ready |
| Setup script | Created `setup-postgresql.sh` | ✅ Ready |

---

## 🚀 NEXT ACTIONS

### 1. Create Database
```bash
./setup-postgresql.sh
# Or manual SQL
```

### 2. Verify PostgreSQL Running
```bash
psql -U postgres -c "SELECT version();"
```

### 3. Run Application
```bash
./gradlew bootRun
```

### 4. Check Logs
```
Look for:
✅ "Executing SQL DDL statement" (migrations running)
✅ "Successfully applied X migrations"
✅ "Tomcat initialized with port(s): 8443"
```

### 5. Test Database
```bash
psql -U postgres -d billsmart_dev -c "SELECT * FROM pg_tables WHERE schemaname='public';"
```

You should see 10 tables created! ✅

---

## 📝 CONFIGURATION DETAILS

### Development (PostgreSQL)
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/billsmart_dev
spring.datasource.username=postgres
spring.datasource.password=Abc123@def

# Flyway ENABLED
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# Validation mode (tables must already exist)
spring.jpa.hibernate.ddl-auto=validate
```

### Tests (H2)
```properties
# Database
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL

# Flyway ENABLED
spring.flyway.enabled=true

# Validation mode (migrations create tables)
spring.jpa.hibernate.ddl-auto=validate
```

---

## ✨ WHY THIS SETUP IS BETTER

✅ **Development matches Production**
   - Same database engine (PostgreSQL)
   - Same SQL syntax
   - Same performance characteristics

✅ **Tests stay isolated**
   - H2 in-memory
   - Fresh database per test
   - No interference with dev data

✅ **Flyway runs automatically**
   - Both dev and tests execute migrations
   - All 10 tables created automatically
   - You just run the app!

✅ **Production ready**
   - Dev environment mirrors production
   - Easy to debug issues
   - No surprises in deployment

---

## 🎯 YOUR DEVELOPMENT WORKFLOW

```
1. Start PostgreSQL (already running)
2. Create billsmart_dev database (./setup-postgresql.sh)
3. Run ./gradlew bootRun
4. Flyway creates all tables automatically
5. Develop APIs
6. Run ./gradlew test (uses H2)
7. Commit and push
8. Production deployment uses PostgreSQL
```

---

## ✅ VERIFIED SETUP

- [x] PostgreSQL running locally
- [x] Flyway enabled for dev
- [x] H2 kept for tests
- [x] application-dev.properties updated
- [x] Setup script created
- [x] Ready to run

---

**You now have the correct setup: PostgreSQL for dev, H2 for tests! 🚀**


