package com.enokdev.spring_boot_migration_starter.service;

import com.enokdev.spring_boot_migration_starter.config.MigrationProperties;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import javax.sql.DataSource;
import java.util.function.Consumer;

@Service
public class LiquibaseMigrationService implements MigrationService {
    
    @Autowired
    private DataSource dataSource;

    @Autowired
    private MigrationProperties properties;

    private static final String DEFAULT_CHANGELOG_PATH = "db/changelog/db.changelog-master.yaml";

    @Override
    public void migrate() {
        if (!properties.isEnabled()) {
            return;
        }

        executeWithLiquibase(liquibase -> {
            try {
                liquibase.update("");
            } catch (LiquibaseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SneakyThrows
    @Override
    public void validate() {
        if (!properties.isEnabled()) {
            return;
        }

        executeWithLiquibase(Liquibase::validate);
    }

    @Override
    public void repair() {
        if (!properties.isEnabled()) {
            return;
        }

        // Liquibase doesn't have a direct repair equivalent
        // We could improve this with a more tailored implementation if needed
        migrate();
    }

    private void executeWithLiquibase(Consumer<Liquibase> action) {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            String changelogPath = properties.getChangeLogPath() != null ?
                    properties.getChangeLogPath() : DEFAULT_CHANGELOG_PATH;

            try (Liquibase liquibase = new Liquibase(
                    changelogPath,
                    new ClassLoaderResourceAccessor(),
                    database)) {
                action.accept(liquibase);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute Liquibase operation", e);
        }
    }
}
