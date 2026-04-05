# BillSmart — Render.com Deployment Guide

## Why Render.com?

| Feature | Details |
|---|---|
| **Free tier** | 1 Web Service + 1 PostgreSQL DB (free, 90-day expiry on DB) |
| **Docker support** | Deploys directly from your `Dockerfile` |
| **Auto-deploy** | Push to GitHub → Render auto-builds & deploys |
| **SSL** | Free HTTPS automatically, no keystore needed |
| **No vendor lock-in** | Standard Docker + PostgreSQL — easy to migrate |

---

## Architecture on Render

```
Mobile App / Browser
       │  HTTPS (Render edge)
       ▼
 Render Web Service (Docker)
  Spring Boot app on port 8080
  profile: prod  — SSL disabled (Render handles it)
       │  JDBC
       ▼
 Render PostgreSQL (managed)
  billsmart database
```

---

## Step-by-Step Deployment

### 1. Push code to GitHub

Make sure your latest code (including `render.yaml`) is committed and pushed:

```bash
git add .
git commit -m "chore: add Render.com deployment config"
git push origin main
```

### 2. Create a Render account

Go to [https://render.com](https://render.com) and sign up (free, use GitHub login for convenience).

### 3. Option A — Automatic via `render.yaml` (Recommended)

1. In Render dashboard → **"New" → "Blueprint"**
2. Connect your GitHub repository
3. Render will detect `render.yaml` and auto-create:
   - A **PostgreSQL** database named `billsmart-db`
   - A **Web Service** named `billsmart-api`
4. Click **"Apply"** — done!

### 4. Option B — Manual Setup

#### 4a. Create PostgreSQL Database

1. Render Dashboard → **"New" → "PostgreSQL"**
2. Name: `billsmart-db`
3. Plan: **Free**
4. Click **"Create Database"**
5. Copy the **Internal Connection String** (looks like `postgres://billsmart_user:password@dpg-xxx-a/billsmart`)

#### 4b. Create Web Service

1. Render Dashboard → **"New" → "Web Service"**
2. Connect your GitHub repo
3. Settings:
   - **Name**: `billsmart-api`
   - **Runtime**: `Docker`
   - **Plan**: Free
   - **Health Check Path**: `/actuator/health`

4. Add **Environment Variables**:

| Key | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | *(from DB → Internal Connection String)* |
| `SPRING_DATASOURCE_USERNAME` | *(from DB → Username)* |
| `SPRING_DATASOURCE_PASSWORD` | *(from DB → Password)* |
| `APP_JWT_SECRET` | *(generate a random 64-char string)* |
| `SERVER_SSL_ENABLED` | `false` |

> **Tip:** Generate a strong JWT secret:
> ```bash
> openssl rand -hex 32
> ```

5. Click **"Create Web Service"**

---

## Environment Variables Reference

| Variable | Required | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | ✅ | Must be `prod` |
| `SPRING_DATASOURCE_URL` | ✅ | JDBC URL from Render DB |
| `SPRING_DATASOURCE_USERNAME` | ✅ | DB username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | DB password |
| `APP_JWT_SECRET` | ✅ | Secret key for JWT signing (min 32 chars) |
| `SERVER_SSL_ENABLED` | ✅ | Set `false` (Render handles SSL) |
| `PORT` | Auto | Render injects this automatically |

---

## How SSL Works on Render

- Your app runs on **HTTP port 8080** inside the container (no keystore needed)
- Render's edge proxy terminates **HTTPS/TLS** and forwards to your container
- Mobile team calls `https://billsmart-api.onrender.com/api/...` — fully encrypted ✅
- `SERVER_SSL_ENABLED=false` disables Spring's internal SSL (no keystore error)

---

## After Deployment

### Your API Base URL

```
https://billsmart-api.onrender.com
```

Update the mobile team's Postman collection base URL to this.

### Check Deployment Logs

Render Dashboard → `billsmart-api` → **"Logs"** tab

### Check Health

```
GET https://billsmart-api.onrender.com/actuator/health
```

Expected:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

---

## Free Tier Limitations

| Limitation | Details |
|---|---|
| **Sleep after inactivity** | Free web services sleep after 15 min of no traffic. First request takes ~30s to wake up |
| **PostgreSQL expiry** | Free DB expires after **90 days** — export data before expiry |
| **Build time** | Free tier builds can take 5-10 min |
| **RAM** | 512 MB RAM — sufficient for BillSmart |

> **Fix sleep issue:** Use a cron job or uptime monitor (e.g., [UptimeRobot](https://uptimerobot.com)) to ping `/actuator/health` every 10 min.

---

## Database Migrations

Flyway runs automatically on startup. All SQL files in `src/main/resources/db/migration/` are applied in order (V1, V2, ... V17).

No manual SQL needed — it's fully automated.

---

## Switching to Another Platform Later

Since we use:
- Standard **Docker** container
- Standard **PostgreSQL** (not Render-specific)
- All config via **environment variables**

Migration is simple:
1. Export DB: `pg_dump` from Render DB
2. Import to new DB
3. Set same env vars on new platform
4. Deploy same Docker image

**Zero code changes required.**

---

## Share with Mobile Team

Once deployed, share:
1. Base URL: `https://billsmart-api.onrender.com`
2. Updated Postman collection with new base URL
3. API documentation (`API_DOCUMENTATION.md`)

---

## Troubleshooting

| Issue | Fix |
|---|---|
| Build fails | Check Render build logs; ensure `Dockerfile` works locally |
| App starts then crashes | Check env vars — especially `SPRING_DATASOURCE_URL` |
| DB connection refused | Use Internal Connection String (not external) for `SPRING_DATASOURCE_URL` |
| SSL error | Ensure `SERVER_SSL_ENABLED=false` is set |
| 404 on `/actuator/health` | Check `SPRING_PROFILES_ACTIVE=prod` is set |

