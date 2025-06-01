# Library Management System - Backend

## Overview
Spring Boot backend for the Library Management System providing RESTful APIs for managing books, users, loans, and orders.

## Technology Stack
- Java 17
- Spring Boot 3.2.5
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Redis
- MinIO
- Maven

## Prerequisites
- JDK 17+
- Maven 3.6+
- PostgreSQL 14+
- Redis 6+
- MinIO (for document storage)

## Setup Instructions

### 1. Database Setup
```sql
CREATE DATABASE library_db_dev;
CREATE USER library_user WITH PASSWORD 'library_password';
GRANT ALL PRIVILEGES ON DATABASE library_db_dev TO library_user;
```

### 2. Running the Application

#### Development Mode
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

#### Test Mode
```bash
mvn spring-boot:run -Dspring.profiles.active=test
```

#### Production Mode
Set environment variables:
- DATABASE_URL
- DATABASE_USERNAME
- DATABASE_PASSWORD
- REDIS_HOST
- REDIS_PORT
- REDIS_PASSWORD
- MINIO_ENDPOINT
- MINIO_ACCESS_KEY
- MINIO_SECRET_KEY
- JWT_SECRET

Then run:
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

## API Documentation
Once the application is running, access the API documentation at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Testing
```bash
mvn test
```

## Building for Production
```bash
mvn clean package
java -jar target/library-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```