package io.github.tky0065.spring_boot_migration_starter.service;

import io.github.tky0065.spring_boot_migration_starter.config.MigrationProperties;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;

@Service
public class FlywayMigrationService implements MigrationService {
    
    @Autowired
    private DataSource dataSource;

    @Autowired
    private MigrationProperties properties;

    @Override
    public void migrate() {
        if (!properties.isEnabled()) {
            return;
        }

        Flyway flyway = configureFlyway();
        flyway.migrate();
    }

    @Override
    public void validate() {
        if (!properties.isEnabled()) {
            return;
        }

        Flyway flyway = configureFlyway();
        flyway.validate();
    }

    @Override
    public void repair() {
        if (!properties.isEnabled()) {
            return;
        }

        Flyway flyway = configureFlyway();
        flyway.repair();
    }

    private Flyway configureFlyway() {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(properties.getLocations().toArray(new String[0]))
                .baselineOnMigrate(properties.isBaselineOnMigrate())
                .validateOnMigrate(properties.isValidateOnMigrate())
                .cleanDisabled(properties.isCleanDisabled())
                .load();
    }
}
