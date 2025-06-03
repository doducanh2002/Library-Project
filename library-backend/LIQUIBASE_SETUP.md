# Liquibase Setup Guide

## Overview
This project supports both Flyway and Liquibase for database migrations. This guide explains how to use Liquibase XML-based migrations.

## Liquibase vs Flyway
- **Flyway**: Uses SQL-based migrations (default)
- **Liquibase**: Uses XML-based migrations (alternative)

## Using Liquibase

### 1. Enable Liquibase Profile
To use Liquibase instead of Flyway, add the `liquibase` profile when running the application:

```bash
mvn spring-boot:run -Dspring.profiles.active=dev,liquibase
```

Or set in your IDE run configuration:
```
--spring.profiles.active=dev,liquibase
```

### 2. Migration Structure
Liquibase migrations are located in `src/main/resources/db/changelog/`:

```
db/changelog/
├── db.changelog-master.xml          # Master changelog file
└── changes/
    ├── 001-initial-schema.xml       # Database schema
    └── 002-initial-data.xml         # Sample data
```

### 3. Changelog Format
Liquibase uses XML format for database changes:

```xml
<changeSet id="001-1" author="library-system">
    <createTable tableName="users">
        <column name="id" type="BIGSERIAL">
            <constraints primaryKey="true"/>
        </column>
        <!-- more columns -->
    </createTable>
</changeSet>
```

## Benefits of Liquibase

1. **Database Independence**: XML format works across different databases
2. **Rollback Support**: Built-in rollback for most operations
3. **Preconditions**: Can check database state before applying changes
4. **Contexts**: Different changes for dev/test/prod environments
5. **Change Documentation**: Built-in documentation in changesets

## Common Liquibase Commands

### Generate SQL Preview
```bash
mvn liquibase:updateSQL
```

### Update Database
```bash
mvn liquibase:update
```

### Rollback Last Change
```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

### View History
```bash
mvn liquibase:history
```

## Adding New Migrations

1. Create a new XML file in `src/main/resources/db/changelog/changes/`
2. Add the include to `db.changelog-master.xml`
3. Follow the naming convention: `{number}-{description}.xml`

Example:
```xml
<changeSet id="003-1" author="your-name">
    <comment>Add new feature table</comment>
    <createTable tableName="features">
        <!-- table definition -->
    </createTable>
</changeSet>
```

## Contexts Usage

Use contexts to apply different changes per environment:

```xml
<changeSet id="002-1" author="library-system" context="dev,test">
    <comment>Insert test data (dev and test only)</comment>
    <!-- changes -->
</changeSet>

<changeSet id="002-2" author="library-system" context="prod">
    <comment>Production-specific changes</comment>
    <!-- changes -->
</changeSet>
```

## Switching Between Flyway and Liquibase

### Use Flyway (default):
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Use Liquibase:
```bash
mvn spring-boot:run -Dspring.profiles.active=dev,liquibase
```

## Troubleshooting

### Liquibase Lock
If Liquibase gets stuck, you may need to release the lock:
```sql
DELETE FROM databasechangeloglock WHERE locked = true;
```

### View Applied Changes
```sql
SELECT * FROM databasechangelog ORDER BY dateexecuted DESC;
```

### Reset Liquibase (Development Only)
```sql
DROP TABLE databasechangelog;
DROP TABLE databasechangeloglock;
```

## Best Practices

1. Always test migrations on a local database first
2. Use meaningful changeset IDs and authors
3. Add comments to explain complex changes
4. Use preconditions when necessary
5. Keep changesets small and focused
6. Never modify existing changesets
7. Use contexts for environment-specific changes