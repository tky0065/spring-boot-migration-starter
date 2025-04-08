package com.enokdev.spring_boot_migration_starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "db.migration")
public class MigrationProperties {
    private String type = "flyway";
    private String location = "classpath:db/migration";
    private boolean enabled = true;
    private boolean baselineOnMigrate = true;
    private boolean validateOnMigrate = true;
}