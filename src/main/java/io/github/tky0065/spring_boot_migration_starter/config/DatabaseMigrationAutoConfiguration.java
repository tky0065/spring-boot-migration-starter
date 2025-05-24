package io.github.tky0065.spring_boot_migration_starter.config;

import io.github.tky0065.spring_boot_migration_starter.service.EntityChangeDetectorService;
import io.github.tky0065.spring_boot_migration_starter.service.FlywayMigrationService;
import io.github.tky0065.spring_boot_migration_starter.service.LiquibaseMigrationService;
import io.github.tky0065.spring_boot_migration_starter.service.MigrationService;
import io.github.tky0065.spring_boot_migration_starter.service.MigrationTemplateGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@AutoConfiguration(before = {FlywayAutoConfiguration.class, LiquibaseAutoConfiguration.class})
@EnableConfigurationProperties(MigrationProperties.class)
public class DatabaseMigrationAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationAutoConfiguration.class);

    private final MigrationProperties properties;

    public DatabaseMigrationAutoConfiguration(MigrationProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing Database Migration Starter with type: {}", properties.getType());
        if (properties.getLocations().isEmpty() && properties.getLocation() != null) {
            properties.getLocations().add(properties.getLocation());
        }

        // Configure Hibernate quote identifiers if needed
        if (properties.isQuoteIdentifiers()) {
            logger.info("Enabling quoted SQL identifiers to handle reserved keywords");
            System.setProperty("spring.jpa.properties.hibernate.globally_quoted_identifiers", "true");
        }

        // Configure proper spring.flyway.enabled or spring.liquibase.enabled based on selection
        if ("liquibase".equalsIgnoreCase(properties.getType())) {
            System.setProperty("spring.flyway.enabled", "false");
            System.setProperty("spring.liquibase.enabled", String.valueOf(properties.isEnabled()));

            // Propager les configurations Liquibase si besoin
            if (properties.getChangeLogPath() != null) {
                System.setProperty("spring.liquibase.change-log", properties.getChangeLogPath());
            }
        } else {
            // Configuration pour Flyway
            System.setProperty("spring.liquibase.enabled", "false");
            System.setProperty("spring.flyway.enabled", String.valueOf(properties.isEnabled()));

            // Propager les paramètres critiques de Flyway
            logger.info("Setting Flyway baselineOnMigrate to: {}", properties.isBaselineOnMigrate());
            System.setProperty("spring.flyway.baseline-on-migrate", String.valueOf(properties.isBaselineOnMigrate()));
            System.setProperty("spring.flyway.validate-on-migrate", String.valueOf(properties.isValidateOnMigrate()));
            System.setProperty("spring.flyway.clean-disabled", String.valueOf(properties.isCleanDisabled()));

            // Propager les locations si définies
            if (!properties.getLocations().isEmpty()) {
                String locations = String.join(",", properties.getLocations());
                logger.info("Setting Flyway locations to: {}", locations);
                System.setProperty("spring.flyway.locations", locations);
            }

            // Configurer les SQL quotes pour Flyway si nécessaire
            if (properties.isQuoteIdentifiers()) {
                logger.info("Setting Flyway SQL quotes for identifiers");
                System.setProperty("spring.flyway.sql-migration-prefix-separator", "__");
                System.setProperty("spring.flyway.sql-quote-identifier", "true");
            }
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public MigrationTemplateGenerator migrationTemplateGenerator(ResourceLoader resourceLoader) {
        logger.info("Configuring MigrationTemplateGenerator");
        MigrationTemplateGenerator generator = new MigrationTemplateGenerator(resourceLoader, properties);
        if (properties.isEnabled()) {
            generator.generateInitialMigrations(properties.getType());
        } else {
            logger.info("Migration template generation is disabled");
        }
        return generator;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "db.migration", name = "auto-generate-migrations", havingValue = "true")
    public EntityChangeDetectorService entityChangeDetectorService(
            ApplicationContext applicationContext,
            MigrationTemplateGenerator migrationTemplateGenerator,
            DataSource dataSource) {
        logger.info("Configuring EntityChangeDetectorService");
        return new EntityChangeDetectorService(
                applicationContext,
                migrationTemplateGenerator,
                properties,
                dataSource);
    }

    /**
     * Event listener to trigger entity change detection and migration generation
     * after the application is fully initialized
     */
    @EventListener(ApplicationReadyEvent.class)
    @ConditionalOnBean(EntityChangeDetectorService.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        logger.info("ApplicationReadyEvent received, checking for entity changes");
        EntityChangeDetectorService detector = event.getApplicationContext().getBean(EntityChangeDetectorService.class);
        detector.detectChangesAndGenerateMigration();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "db.migration", name = "type", havingValue = "flyway", matchIfMissing = true)
    @ConditionalOnMissingBean(MigrationService.class)
    public MigrationService flywayMigrationService() {
        logger.info("Configuring Flyway migration service");
        return new FlywayMigrationService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "db.migration", name = "type", havingValue = "liquibase")
    @ConditionalOnMissingBean(MigrationService.class)
    public MigrationService liquibaseMigrationService() {
        logger.info("Configuring Liquibase migration service");
        return new LiquibaseMigrationService();
    }
}

