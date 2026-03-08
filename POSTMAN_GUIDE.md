# BillSmart API - Postman Guide

## Prerequisites
- Application running with dev profile: `./gradlew bootRun --args='--spring.profiles.active=dev'`
- Postman installed
- A restaurant already created (you need restaurantId to create food)

---

## 1. Create Restaurant (Required First)

Before creating food, you need to create a restaurant. Here's how:

### Endpoint
```
POST https://localhost:8443/restaurants/create
```

### Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "outletName": "Pizza Palace",
  "displayname": "Pizza Palace Restaurant",
  "outletManager": "John Doe",
  "storeAddress": {
    "building": "Building A",
    "street": "Main Street",
    "storelocation": "Downtown",
    "zipCode": "12345"
  }
}
```

### Response (201 Created)
```json
{
  "restroId": 1,
  "outletName": "Pizza Palace",
  "displayname": "Pizza Palace Restaurant",
  "outletManager": "John Doe",
  "storeAddress": {
    "building": "Building A",
    "street": "Main Street",
    "storelocation": "Downtown",
    "zipCode": "12345"
  },
  "storeLogo": {
    "logoUrl": "",
    "mediaType": ""
  },
  "foods": []
}
```

**Note:** Save the `restroId` (in this example: `1`) - you'll need it to create food.

---

## 2. Create Food Item ⭐ (What You Asked For)

### Endpoint
```
POST https://localhost:8443/restaurants/{restroId}/foods
```

Replace `{restroId}` with the actual restaurant ID (e.g., `1`)

**Full Example:**
```
POST https://localhost:8443/restaurants/1/foods
```

### Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "name": "Margherita Pizza",
  "price": 299.99
}
```

### Response (201 Created)
```json
{
  "id": 1,
  "name": "Margherita Pizza",
  "price": 299.99,
  "restroId": 1
}
```

---

## 3. Get Food Item

### Endpoint
```
GET https://localhost:8443/restaurants/{restroId}/foods/{id}
```

**Full Example:**
```
GET https://localhost:8443/restaurants/1/foods/1
```

### Response (200 OK)
```json
{
  "id": 1,
  "name": "Margherita Pizza",
  "price": 299.99,
  "restroId": 1
}
```

---

## Example Workflow in Postman

### Step 1: Create a Restaurant
1. Open Postman
2. Create new request
3. Method: **POST**
4. URL: `https://localhost:8443/restaurants/create`
5. Go to **Body** tab → **raw** → **JSON**
6. Paste:
```json
{
  "outletName": "Pizza Palace",
  "displayname": "Pizza Palace Restaurant",
  "outletManager": "John Doe",
  "storeAddress": {
    "building": "Building A",
    "street": "Main Street",
    "storelocation": "Downtown",
    "zipCode": "12345"
  }
}
```
7. Click **Send**
8. Copy the `restroId` from response (e.g., `1`)

### Step 2: Create Food Items
1. Create new request
2. Method: **POST**
3. URL: `https://localhost:8443/restaurants/1/foods` (replace `1` with your restroId)
4. Go to **Body** tab → **raw** → **JSON**
5. Paste (modify as needed):
```json
{
  "name": "Margherita Pizza",
  "price": 299.99
}
```
6. Click **Send**
7. Food is created! ✅

### Step 3: Create More Foods (Optional)
Repeat Step 2 with different food items:

**Example 1:**
```json
{
  "name": "Pepperoni Pizza",
  "price": 349.99
}
```

**Example 2:**
```json
{
  "name": "Cheese Burger",
  "price": 199.99
}
```

**Example 3:**
```json
{
  "name": "Biryani",
  "price": 249.50
}
```

---

## Common Field Validation Errors

### Error: Missing or Empty `name`
```json
{
  "name": "",
  "price": 299.99
}
```
**Response:** 400 Bad Request - "name must not be blank"

### Error: Missing `price`
```json
{
  "name": "Margherita Pizza"
}
```
**Response:** 400 Bad Request - "price must not be null"

### Error: Invalid Restaurant ID
```
POST https://localhost:8443/restaurants/999/foods
```
**Response:** 400 Bad Request - "Restaurant not found: 999"

---

## SSL Certificate Warning in Postman

If you get an SSL certificate warning:

1. Go to **Postman Settings** (gear icon)
2. Go to **General** tab
3. Find **SSL certificate verification**
4. Toggle **OFF** (for local development only)
5. Click **Update**

---

## Database Verification

After creating food items, verify in H2 Console:

1. Open browser: https://localhost:8443/h2-console
2. JDBC URL: `jdbc:h2:mem:devdb`
3. User: `sa`
4. Password: (leave blank)
5. Click **Connect**
6. Run query:
```sql
SELECT * FROM food;
```

You should see your created food items! ✅

---

## Quick Reference

| Action | Method | Endpoint | Body |
|--------|--------|----------|------|
| Create Restaurant | POST | `/restaurants/create` | See Example 1 |
| Create Food | POST | `/restaurants/{id}/foods` | `{ "name": "...", "price": 299.99 }` |
| Get Food | GET | `/restaurants/{restroId}/foods/{id}` | None |

---

## Sample Test Data

### Restaurant 1: Italian Restaurant
```json
{
  "outletName": "Bella Italia",
  "displayname": "Bella Italia Pizzeria",
  "outletManager": "Marco Rossi",
  "storeAddress": {
    "building": "101",
    "street": "Via Roma",
    "storelocation": "City Center",
    "zipCode": "10001"
  }
}
```

Foods for Restaurant 1:
- Name: "Classic Margherita", Price: 299.99
- Name: "Pepperoni Deluxe", Price: 349.99
- Name: "Quattro Formaggi", Price: 399.99

### Restaurant 2: Indian Restaurant
```json
{
  "outletName": "Taste of India",
  "displayname": "Taste of India Restaurant",
  "outletManager": "Raj Patel",
  "storeAddress": {
    "building": "202",
    "street": "Curry Lane",
    "storelocation": "Downtown",
    "zipCode": "10002"
  }
}
```

Foods for Restaurant 2:
- Name: "Chicken Biryani", Price: 249.99
- Name: "Butter Chicken", Price: 299.99
- Name: "Paneer Tikka", Price: 279.99

---

## Troubleshooting

### Connection Refused
- Make sure app is running: `./gradlew bootRun --args='--spring.profiles.active=dev'`
- Check if port 8443 is correct

### SSL Certificate Error
- Disable SSL verification in Postman (see above)
- Or add exception to your system

### 404 Not Found
- Check endpoint URL is correct
- Check restaurant ID exists
- Check path parameters (use `/` not `\`)

### 400 Bad Request
- Check JSON format is valid
- Check all required fields are provided
- Check field types (price should be number, not string)

---

## Notes

✅ **App runs on:** https://localhost:8443 (HTTPS)
✅ **H2 Console:** https://localhost:8443/h2-console
✅ **Database:** H2 in-memory (dev profile)
✅ **Data persists:** During current session only (H2 in-memory)
⚠️ **Data lost on:** App restart (normal for H2 in-memory)

If you want persistent data during development, switch to PostgreSQL in `application-dev.properties`.

---

Generated: March 7, 2026
Version: 1.0

