package io.github.tky0065.spring_boot_migration_starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "db.migration")
public class MigrationProperties {
    /**
     * Type of migration tool to use (flyway or liquibase)
     */
    private String type = "flyway";

    /**
     * Migration script locations (can be a single string or a list)
     */
    private List<String> locations = new ArrayList<>();

    /**
     * Single location for backward compatibility
     */
    private String location;

    /**
     * Whether database migration is enabled
     */
    private boolean enabled = true;

    /**
     * Whether to baseline on migrate (for Flyway)
     */
    private boolean baselineOnMigrate = true;

    /**
     * Whether to validate on migrate (for Flyway)
     */
    private boolean validateOnMigrate = true;

    /**
     * Whether clean operation is disabled (for Flyway)
     */
    private boolean cleanDisabled = true;

    /**
     * Schema name for Flyway migrations
     */
    private String schema;

    /**
     * The baseline version (for Flyway)
     */
    private String baselineVersion = "1";

    /**
     * Changelog path for Liquibase
     */
    private String changeLogPath;

    /**
     * Contexts for Liquibase
     */
    private String contexts;

    /**
     * Labels for Liquibase
     */
    private String labels;

    /**
     * Additional Flyway configuration properties
     */
    private Map<String, String> flywayProperties = new HashMap<>();

    /**
     * Additional Liquibase configuration properties
     */
    private Map<String, String> liquibaseProperties = new HashMap<>();

    /**
     * Whether SQL identifiers should be quoted
     * This helps when using reserved keywords as table names
     */
    private boolean quoteIdentifiers = false;

    /**
     * Whether to automatically generate migration scripts from entity changes
     */
    private boolean autoGenerateMigrations = false;

    /**
     * Directory where to save generated migrations
     */
    private String generatedMigrationsPath = "src/main/resources/db/migration";
}
