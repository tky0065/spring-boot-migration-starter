package com.enokdev.spring_boot_migration_starter.service;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;

public class FlywayMigrationService implements MigrationService {
    
    @Autowired
    private DataSource dataSource;

    @Override
    public void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();
        flyway.migrate();
    }

    @Override
    public void validate() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();
        flyway.validate();
    }

    @Override
    public void repair() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();
        flyway.repair();
    }
}