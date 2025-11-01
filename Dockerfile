# Multi-stage Dockerfile
# Build stage
FROM gradle:8-jdk17 AS builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . /home/gradle/project
RUN gradle --no-daemon bootJar

# Run stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Copy entrypoint script (created for debugging env) and make executable
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Install curl for healthchecks and remove cache (done as root)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user and give ownership of the app file
RUN groupadd -r app && useradd -r -g app app \
    && chown app:app /app/app.jar /usr/local/bin/docker-entrypoint.sh

# Switch to non-root user
USER app

EXPOSE 8080 8081
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
