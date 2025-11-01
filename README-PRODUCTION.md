# Running the application with PostgreSQL (production profile) - local guide

This guide shows how to run PostgreSQL locally with Docker Compose and start the application using the `prod` profile which uses PostgreSQL and Flyway for migrations.

Prerequisites
- Docker & Docker Compose installed
- Java 17 installed
- gradle wrapper is included (use `gradlew.bat` on Windows)

Steps

1. Copy the example env file:

```powershell
copy .env.example .env
```

2. Bring up the stack with Docker Compose (this will run Postgres and the app container):

```powershell
docker-compose up -d --build
```

The `app` service in docker-compose will:
- build the application image using the provided `Dockerfile` (multi-stage)
- start the app with `SPRING_PROFILES_ACTIVE=prod`
- perform a healthcheck against `http://localhost:8081/actuator/health`

3. Alternative: run only Postgres and start the app locally (build jar then run):

Start Postgres:
```powershell
docker-compose up -d db
```

Build the application jar:
```powershell
gradlew.bat --no-daemon bootJar
```

Run the app using the `prod` profile (example with environment variables):
```powershell
set DATABASE_URL=jdbc:postgresql://localhost:5432/mydb
set DATABASE_USERNAME=myuser
set DATABASE_PASSWORD=mypassword
gradlew.bat bootRun --args="--spring.profiles.active=prod"
```

Or run the built jar:
```powershell
java -jar build\libs\BillSmart-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

What happens
- Flyway will automatically run on application startup (default Spring Boot behavior when flyway-core is on the classpath), applying migrations from `src/main/resources/db/migration`.
- The application will use the PostgreSQL database defined by `DATABASE_URL`.

Important safety notes
- Do NOT commit real credentials to the repo. Use environment variables or a secret manager.
- `spring.jpa.hibernate.ddl-auto=none` in `application-prod.properties` — do not rely on Hibernate to change the schema automatically in production.
- Consider enabling SSL/TLS for DB connections in production depending on your environment.

Next steps / production hardening
- Add health checks and monitoring.
- Add a production CI/CD pipeline that runs migrations, builds the image, and deploys the app.
- Use a secret manager or Docker/Kubernetes secrets for credentials.
