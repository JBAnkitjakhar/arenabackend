# Dockerfile - Fixed for timezone and Render PORT environment variable issue

# Use official OpenJDK 21 image
FROM openjdk:21-jdk-slim

# Set timezone to UTC
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Install curl for health checks
RUN apt-get update && apt-get install -y curl tzdata && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better Docker layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ ./src/

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown -R spring:spring /app
USER spring

# Expose port 8080 (Render will map this to external port)
EXPOSE 8080

# Health check (optional but recommended for Render)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# ✅ UPDATED: Run the application with production profile AND timezone flag
CMD java -Duser.timezone=UTC -Dspring.profiles.active=prod -jar target/algoarena-backend-0.0.1-SNAPSHOT.jar