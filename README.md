# Spring Boot Migration Starter Documentation

## Overview
Spring Boot Migration Starter is a library that provides seamless integration of database migration tools (Flyway and Liquibase) in Spring Boot applications.

## Table of Contents
- [Installation](#installation)
- [Features](#features)
- [Configuration](#configuration)
- [Usage](#usage)
- [Migration Tools](#migration-tools)
- [Advanced Usage](#advanced-usage)
- [Best Practices](#best-practices)

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>spring-boot-migration-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Features
- Automatic configuration for both Flyway and Liquibase
- Support for multiple database types
- Migration script templates
- Configurable migration strategies
- Spring Boot auto-configuration support

## Configuration

### Basic Configuration (application.yml)
```yaml
db:
  migration:
    type: flyway  # or 'liquibase'
    enabled: true
    locations: classpath:db/migration
```

### Advanced Configuration
```yaml
db:
  migration:
    type: flyway
    baseline-on-migrate: true
    validate-on-migrate: true
    clean-disabled: true
    locations:
      - classpath:db/migration
      - classpath:db/specific
```

## Usage

### Basic Migration Example

1. **Create Migration Directory**:
```bash
mkdir -p src/main/resources/db/migration
```

2. **Create First Migration**:
```sql
-- V1__init_schema.sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Liquibase Example
```yaml
# db/changelog/db.changelog-master.yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: enokdev
      changes:
        - createTable:
            tableName: users
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
```

## Migration Tools

### Flyway
- Version-based migrations
- SQL and Java-based migrations
- Automatic schema creation
- Baseline support

### Liquibase
- XML, YAML, JSON, or SQL formats
- Rollback support
- Contextual migrations
- Preconditions

## Advanced Usage

### Custom Migration Scripts
```java
@Component
public class CustomMigration {
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Custom migration logic
            flyway.baseline();
            flyway.migrate();
        };
    }
}
```

## Best Practices

1. **Version Control**
   - Keep migrations in version control
   - Use descriptive names
   - Never modify existing migrations

2. **Migration Strategy**
   - One change per migration
   - Test migrations thoroughly
   - Use meaningful version numbers

3. **Security**
   - Secure sensitive data
   - Use environment variables
   - Implement proper access control

## Support
- GitHub Issues: [Project Issues](https://github.com/tky0065/spring-boot-migration-starter/issues)
- Documentation: [Wiki](https://github.com/tky0065/spring-boot-migration-starter/wiki)

## License
MIT License - See [LICENSE](http://www.opensource.org/licenses/mit-license.php) for details.# Spring Boot Migration Starter Documentation

## Overview
Spring Boot Migration Starter is a library that provides seamless integration of database migration tools (Flyway and Liquibase) in Spring Boot applications.

## Table of Contents
- [Installation](#installation)
- [Features](#features)
- [Configuration](#configuration)
- [Usage](#usage)
- [Migration Tools](#migration-tools)
- [Advanced Usage](#advanced-usage)
- [Best Practices](#best-practices)

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>spring-boot-migration-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Features
- Automatic configuration for both Flyway and Liquibase
- Support for multiple database types
- Migration script templates
- Configurable migration strategies
- Spring Boot auto-configuration support

## Configuration

### Basic Configuration (application.yml)
```yaml
db:
  migration:
    type: flyway  # or 'liquibase'
    enabled: true
    locations: classpath:db/migration
```

### Advanced Configuration
```yaml
db:
  migration:
    type: flyway
    baseline-on-migrate: true
    validate-on-migrate: true
    clean-disabled: true
    locations:
      - classpath:db/migration
      - classpath:db/specific
```

## Usage

### Basic Migration Example

1. **Create Migration Directory**:
```bash
mkdir -p src/main/resources/db/migration
```

2. **Create First Migration**:
```sql
-- V1__init_schema.sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Liquibase Example
```yaml
# db/changelog/db.changelog-master.yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: enokdev
      changes:
        - createTable:
            tableName: users
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
```

## Migration Tools

### Flyway
- Version-based migrations
- SQL and Java-based migrations
- Automatic schema creation
- Baseline support

### Liquibase
- XML, YAML, JSON, or SQL formats
- Rollback support
- Contextual migrations
- Preconditions

## Advanced Usage

### Custom Migration Scripts
```java
@Component
public class CustomMigration {
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Custom migration logic
            flyway.baseline();
            flyway.migrate();
        };
    }
}
```

## Best Practices

1. **Version Control**
   - Keep migrations in version control
   - Use descriptive names
   - Never modify existing migrations

2. **Migration Strategy**
   - One change per migration
   - Test migrations thoroughly
   - Use meaningful version numbers

3. **Security**
   - Secure sensitive data
   - Use environment variables
   - Implement proper access control

## Support
- GitHub Issues: [Project Issues](https://github.com/tky0065/spring-boot-migration-starter/issues)
- Documentation: [Wiki](https://github.com/tky0065/spring-boot-migration-starter/wiki)

## License
MIT License - See [LICENSE](http://www.opensource.org/licenses/mit-license.php) for details.
