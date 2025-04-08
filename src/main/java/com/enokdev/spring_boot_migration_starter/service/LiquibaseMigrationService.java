package com.enokdev.spring_boot_migration_starter.service;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;

public class LiquibaseMigrationService implements MigrationService {
    
    @Autowired
    private DataSource dataSource;

    @Override
    public void migrate() {
        try {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
            Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.yaml", 
                    new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform Liquibase migration", e);
        }
    }

    @Override
    public void validate() {
        try {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
            Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.yaml", 
                    new ClassLoaderResourceAccessor(), database);
            liquibase.validate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate Liquibase changelog", e);
        }
    }

    @Override
    public void repair() {
        // Liquibase doesn't have a direct repair equivalent
        migrate();
    }
}