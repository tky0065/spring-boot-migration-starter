package com.enokdev.spring_boot_migration_starter.config;

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

