package io.github.tky0065.spring_boot_migration_starter.service;

import io.github.tky0065.spring_boot_migration_starter.config.MigrationProperties;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;

@Service
public class FlywayMigrationService implements MigrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlywayMigrationService.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MigrationProperties properties;

    @Override
    public void migrate() {
        if (!properties.isEnabled()) {
            logger.info("Flyway migration is disabled");
            return;
        }

        logger.info("Starting Flyway database migration");
        Flyway flyway = configureFlyway();
        flyway.migrate();
        logger.info("Flyway migration completed successfully");
    }

    @Override
    public void validate() {
        if (!properties.isEnabled()) {
            logger.info("Flyway validation is disabled");
            return;
        }

        logger.info("Validating database schema with Flyway");
        Flyway flyway = configureFlyway();
        flyway.validate();
        logger.info("Flyway validation completed successfully");
    }

    @Override
    public void repair() {
        if (!properties.isEnabled()) {
            logger.info("Flyway repair is disabled");
            return;
        }

        logger.info("Repairing database schema with Flyway");
        Flyway flyway = configureFlyway();
        flyway.repair();
        logger.info("Flyway repair completed successfully");
    }

    private Flyway configureFlyway() {
        logger.debug("Configuring Flyway with the following properties: {}", properties);

        org.flywaydb.core.api.configuration.FluentConfiguration configuration = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(properties.isBaselineOnMigrate())
                .validateOnMigrate(properties.isValidateOnMigrate())
                .cleanDisabled(properties.isCleanDisabled());

        // Set locations if provided
        if (!properties.getLocations().isEmpty()) {
            configuration.locations(properties.getLocations().toArray(new String[0]));
        } else if (StringUtils.hasText(properties.getLocation())) {
            configuration.locations(properties.getLocation());
        }

        // Set schema if provided
        if (StringUtils.hasText(properties.getSchema())) {
            configuration.schemas(properties.getSchema());
        }

        // Set baselineVersion if provided
        if (StringUtils.hasText(properties.getBaselineVersion())) {
            configuration.baselineVersion(properties.getBaselineVersion());
        }

        // Apply additional properties if provided
        for (Map.Entry<String, String> entry : properties.getFlywayProperties().entrySet()) {
            configuration.configuration(Map.of(entry.getKey(), entry.getValue()));
        }

        return configuration.load();
    }
}
