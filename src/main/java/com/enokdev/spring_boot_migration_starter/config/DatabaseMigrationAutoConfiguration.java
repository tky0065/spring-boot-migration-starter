package com.enokdev.spring_boot_migration_starter.config;

import com.enokdev.spring_boot_migration_starter.service.FlywayMigrationService;
import com.enokdev.spring_boot_migration_starter.service.LiquibaseMigrationService;
import com.enokdev.spring_boot_migration_starter.service.MigrationService;
import com.enokdev.spring_boot_migration_starter.service.MigrationTemplateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@AutoConfiguration
@EnableConfigurationProperties(MigrationProperties.class)
public class DatabaseMigrationAutoConfiguration {

    @Autowired
    private MigrationProperties properties;

    @Bean
    public MigrationTemplateGenerator migrationTemplateGenerator(ResourceLoader resourceLoader) {
        MigrationTemplateGenerator generator = new MigrationTemplateGenerator(resourceLoader);
        generator.generateInitialMigrations(properties.getType());
        return generator;
    }

    @Bean
    @ConditionalOnProperty(prefix = "db.migration", name = "type", havingValue = "flyway", matchIfMissing = true)
    public MigrationService flywayMigrationService() {
        return new FlywayMigrationService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "db.migration", name = "type", havingValue = "liquibase")
    public MigrationService liquibaseMigrationService() {
        return new LiquibaseMigrationService();
    }
}