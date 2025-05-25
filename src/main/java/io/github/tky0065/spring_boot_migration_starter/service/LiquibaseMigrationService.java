package io.github.tky0065.spring_boot_migration_starter.service;

import io.github.tky0065.spring_boot_migration_starter.config.MigrationProperties;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Consumer;

@Service
public class LiquibaseMigrationService implements MigrationService {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseMigrationService.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MigrationProperties properties;

    private static final String DEFAULT_CHANGELOG_PATH = "db/changelog/db.changelog-master.yaml";

    @Override
    public void migrate() {
        if (!properties.isEnabled()) {
            logger.info("Liquibase migration is disabled");
            return;
        }

        logger.info("Starting Liquibase database migration");
        executeWithLiquibase(liquibase -> {
            try {
                Contexts contexts = StringUtils.hasText(properties.getContexts()) ?
                        new Contexts(properties.getContexts()) : new Contexts();

                LabelExpression labelExpression = StringUtils.hasText(properties.getLabels()) ?
                        new LabelExpression(properties.getLabels()) : new LabelExpression();

                liquibase.update(contexts, labelExpression);
                logger.info("Liquibase migration completed successfully");
            } catch (LiquibaseException e) {
                logger.error("Failed to update database schema", e);
                throw new RuntimeException("Failed to update database schema", e);
            }
        });
    }

    @Override
    public void validate() {
        if (!properties.isEnabled()) {
            logger.info("Liquibase validation is disabled");
            return;
        }

        logger.info("Validating database schema with Liquibase");
        executeWithLiquibase(liquibase -> {
            try {
                liquibase.validate();
                logger.info("Liquibase validation completed successfully");
            } catch (LiquibaseException e) {
                logger.error("Database validation failed", e);
                throw new RuntimeException("Database validation failed", e);
            }
        });
    }

    @Override
    public void repair() {
        if (!properties.isEnabled()) {
            logger.info("Liquibase repair is disabled");
            return;
        }

        logger.info("Repairing database schema with Liquibase");
        executeWithLiquibase(liquibase -> {
            try {
                // Liquibase doesn't have a direct repair method like Flyway
                // Instead, we can clear checksums which is similar in function
                liquibase.clearCheckSums();
                logger.info("Liquibase checksums cleared successfully");
            } catch (LiquibaseException e) {
                logger.error("Failed to clear checksums", e);
                throw new RuntimeException("Failed to clear checksums", e);
            }
        });
    }

    private void executeWithLiquibase(Consumer<Liquibase> liquibaseConsumer) {
        String changeLogPath = StringUtils.hasText(properties.getChangeLogPath()) ?
                properties.getChangeLogPath() : DEFAULT_CHANGELOG_PATH;

        logger.debug("Using changelog path: {}", changeLogPath);

        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            // Apply any custom properties - using Liquibase API correctly
            for (var entry : properties.getLiquibaseProperties().entrySet()) {
                // La méthode setDatabaseProperty n'existe pas dans Database
                // Utilisez les méthodes spécifiques selon les propriétés ou ignorez-les
                if ("defaultSchemaName".equals(entry.getKey())) {
                    database.setDefaultSchemaName(entry.getValue());
                } else if ("defaultCatalogName".equals(entry.getKey())) {
                    database.setDefaultCatalogName(entry.getValue());
                } else if ("outputDefaultSchema".equals(entry.getKey())) {
                    database.setOutputDefaultSchema(Boolean.parseBoolean(entry.getValue()));
                } else if ("outputDefaultCatalog".equals(entry.getKey())) {
                    database.setOutputDefaultCatalog(Boolean.parseBoolean(entry.getValue()));
                } else if ("liquibaseTablespaceName".equals(entry.getKey())) {
                    database.setLiquibaseTablespaceName(entry.getValue());
                } else {
                    logger.warn("La propriété Liquibase '{}' n'est pas supportée directement", entry.getKey());
                }
            }

            if (StringUtils.hasText(properties.getSchema())) {
                database.setDefaultSchemaName(properties.getSchema());
                logger.debug("Using schema: {}", properties.getSchema());
            }

            Liquibase liquibase = new Liquibase(changeLogPath, new ClassLoaderResourceAccessor(), database);
            liquibaseConsumer.accept(liquibase);
        } catch (Exception e) {
            logger.error("Error executing Liquibase operation", e);
            throw new RuntimeException("Error executing Liquibase operation", e);
        }
    }
}
