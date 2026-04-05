#!/bin/sh
set -eu

# Print key environment variables so we can see exact values (wrapped in delimiters)
echo "==== CONTAINER ENV AT STARTUP ===="
printf "PORT=[%s]\n" "${PORT:-8080}" || true
printf "SPRING_PROFILES_ACTIVE=[%s]\n" "${SPRING_PROFILES_ACTIVE:-}" || true
printf "SPRING_DATASOURCE_USERNAME=[%s]\n" "${SPRING_DATASOURCE_USERNAME:-}" || true
printf "DATABASE_USERNAME=[%s]\n" "${DATABASE_USERNAME:-}" || true
printf "SPRING_DATASOURCE_PASSWORD=[%s]\n" "${SPRING_DATASOURCE_PASSWORD:-}" || true
printf "SPRING_DATASOURCE_URL=[%s]\n" "${SPRING_DATASOURCE_URL:-}" || true

echo "Starting application..."
exec java -jar app.jar

