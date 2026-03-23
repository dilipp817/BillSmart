#!/bin/bash
# PostgreSQL Setup Script for BillSmart Development
# Run this to create the development database

echo "🔧 Setting up PostgreSQL database for BillSmart..."

# Create database
psql -U postgres -h localhost <<EOF
CREATE DATABASE billsmart_dev
    WITH
    ENCODING = 'UTF8'
    LOCALE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- Connect to the new database and create schema
\c billsmart_dev

-- Create public schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS public;

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- Verify
SELECT datname FROM pg_database WHERE datname = 'billsmart_dev';
EOF

echo "✅ Database setup complete!"
echo ""
echo "📝 Next steps:"
echo "1. Run: ./gradlew bootRun"
echo "2. Flyway will automatically run all migrations (V1-V15)"
echo "3. Connect to: jdbc:postgresql://localhost:5432/billsmart_dev"

