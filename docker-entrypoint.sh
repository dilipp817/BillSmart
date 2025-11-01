#!/bin/sh
set -eu

# Print key environment variables so we can see exact values (wrapped in delimiters)
echo "==== CONTAINER ENV AT STARTUP ===="
printf "SPRING_DATASOURCE_USERNAME=[%s]\n" "${SPRING_DATASOURCE_USERNAME:-}" || true
printf "DATABASE_USERNAME=[%s]\n" "${DATABASE_USERNAME:-}" || true
printf "SPRING_DATASOURCE_PASSWORD=[%s]\n" "${SPRING_DATASOURCE_PASSWORD:-}" || true
printf "SPRING_DATASOURCE_URL=[%s]\n" "${SPRING_DATASOURCE_URL:-}" || true

echo "Starting application..."
exec java -jar app.jar

