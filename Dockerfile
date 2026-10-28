# Build stage: Maven with JDK 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Build application artifact
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage: Lightweight Alpine JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Ensure database directory exists for persistent H2 storage
RUN mkdir -p data

# Copy compiled JAR
COPY --from=build /app/target/basic-web-0.0.1-SNAPSHOT.jar app.jar

# Support cloud dynamic port allocation (e.g. Render, Railway, Cloud Run)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]

