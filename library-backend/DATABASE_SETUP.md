# Database Setup Guide

## Overview
This project uses PostgreSQL as the main database with Flyway for database migrations.

## Prerequisites
- Docker and Docker Compose installed
- Java 17+
- Maven 3.6+

## Quick Start

1. **Start the database services:**
   ```bash
   docker-compose up -d
   ```
   This will start:
   - PostgreSQL on port 5432
   - Redis on port 6379
   - MinIO on ports 9000 (API) and 9001 (Console)

2. **Verify services are running:**
   ```bash
   docker-compose ps
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   Flyway will automatically run the migrations on startup.

## Database Configuration

### Development Environment
- **Database:** library_db_dev
- **Username:** library_user
- **Password:** library_password
- **Host:** localhost
- **Port:** 5432

### Test Environment
Uses H2 in-memory database with PostgreSQL compatibility mode.

## Flyway Migrations

Migrations are located in `src/main/resources/db/migration/`:
- `V1__Initial_Schema.sql` - Creates all database tables and indexes
- `V2__Initial_Data.sql` - Inserts sample data for development

### Default Users
After running migrations, these users will be available:

1. **Admin User**
   - Username: admin
   - Password: admin123
   - Role: ROLE_ADMIN

2. **Librarian User**
   - Username: librarian
   - Password: librarian123
   - Role: ROLE_LIBRARIAN

3. **Test User**
   - Username: testuser
   - Password: user123
   - Role: ROLE_USER

## MinIO Configuration
- **Access Key:** minioadmin
- **Secret Key:** minioadmin
- **Console URL:** http://localhost:9001
- **API URL:** http://localhost:9000

## Useful Commands

### Connect to PostgreSQL
```bash
docker exec -it library_postgres psql -U library_user -d library_db_dev
```

### View Redis data
```bash
docker exec -it library_redis redis-cli
```

### Stop all services
```bash
docker-compose down
```

### Stop and remove all data
```bash
docker-compose down -v
```

## Troubleshooting

### Port already in use
If you get a port conflict error, you can change the ports in `docker-compose.yml`.

### Database connection issues
1. Ensure Docker services are running: `docker-compose ps`
2. Check logs: `docker-compose logs postgres`
3. Verify connection settings in `application-dev.yml`

### Flyway migration failures
1. Check migration syntax in SQL files
2. Ensure migrations follow naming convention: `V{version}__{description}.sql`
3. Review Flyway logs in application output