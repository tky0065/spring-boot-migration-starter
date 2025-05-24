package io.github.tky0065.spring_boot_migration_starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

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
     * Changelog path for Liquibase
     */
    private String changeLogPath;

    /**
     * Whether SQL identifiers should be quoted
     * This helps when using reserved keywords as table names
     */
    private boolean quoteIdentifiers = false;

    /**
     * Whether to auto-generate migration files when entity changes are detected
     */
    private boolean autoGenerateMigrations = false;

    /**
     * Base packages to scan for JPA entities
     */
    private List<String> entityBasePackages = new ArrayList<>();

    /**
     * Main application class (used to detect base package if not specified)
     */
    private String mainApplicationClass;

    /**
     * Hibernate dialect to use for schema generation
     */
    private String dialect = "org.hibernate.dialect.H2Dialect";

    /**
     * Directory for Liquibase changelog files
     */
    private String liquibaseChangelogDir = "src/main/resources/db/changelog";

    /**
     * Master changelog file for Liquibase
     */
    private String liquibaseMasterChangelog = "src/main/resources/db/changelog/db.changelog-master.xml";

    /**
     * Setter for single location as string for backward compatibility
     */
    public void setLocation(String location) {
        this.locations.clear();
        this.locations.add(location);
    }

    /**
     * Get the first location or a default if none exists
     */
    public String getLocation() {
        return locations.isEmpty() ? "classpath:db/migration" : locations.get(0);
    }
}

