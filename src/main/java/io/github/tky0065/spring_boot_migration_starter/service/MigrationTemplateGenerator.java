package io.github.tky0065.spring_boot_migration_starter.service;

import io.github.tky0065.spring_boot_migration_starter.config.MigrationProperties;
import org.springframework.core.io.ResourceLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationTemplateGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(MigrationTemplateGenerator.class);
    private static final String FLYWAY_DEFAULT_PATH = "src/main/resources/db/migration";
    private static final String LIQUIBASE_DEFAULT_PATH = "src/main/resources/db/changelog";

    private final ResourceLoader resourceLoader;
    private final MigrationProperties properties;

    public MigrationTemplateGenerator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.properties = null; // Will be used in constructor below
    }

    public MigrationTemplateGenerator(ResourceLoader resourceLoader, MigrationProperties properties) {
        this.resourceLoader = resourceLoader;
        this.properties = properties;
    }
    
    public void generateInitialMigrations(String type) {
        logger.info("Generating initial migration templates for type: {}", type);
        if ("flyway".equalsIgnoreCase(type)) {
            generateFlywayMigration();
        } else if ("liquibase".equalsIgnoreCase(type)) {
            generateLiquibaseMigration();
        } else {
            logger.warn("Unknown migration type: {}. No templates will be generated.", type);
        }
    }

    /**
     * Generate a new Flyway migration file with the provided SQL content
     * @param description Description to include in the filename
     * @param sql SQL content to write
     * @return Path to the generated file or null if generation failed
     */
    public Path generateNewFlywayMigration(String description, String sql) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String filename = String.format("V%s__%s.sql", timestamp, description.replace(' ', '_'));

        String migrationPath = getMigrationPath("flyway");
        createDirectory(migrationPath);

        Path filePath = Paths.get(migrationPath, filename);
        writeFile(filePath, sql);

        logger.info("Generated new Flyway migration: {}", filePath);
        return filePath;
    }

    /**
     * Generate a new Liquibase changelog file
     * @param id ID for the changeset
     * @param author Author name
     * @param yamlContent YAML content to write
     * @return Path to the generated file or null if generation failed
     */
    public Path generateNewLiquibaseChangelog(String id, String author, String yamlContent) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String filename = String.format("changelog-%s.yaml", timestamp);

        String changelogPath = getMigrationPath("liquibase");
        createDirectory(changelogPath);

        Path filePath = Paths.get(changelogPath, filename);
        writeFile(filePath, yamlContent);

        // Update master changelog to include this file
        updateLiquibaseMasterChangelog(filename);

        logger.info("Generated new Liquibase changelog: {}", filePath);
        return filePath;
    }

    private void updateLiquibaseMasterChangelog(String filename) {
        String changelogPath = getMigrationPath("liquibase");
        Path masterPath = Paths.get(changelogPath, "db.changelog-master.yaml");

        if (Files.exists(masterPath)) {
            try {
                String content = new String(Files.readAllBytes(masterPath));
                if (!content.contains(filename)) {
                    String includeEntry = "\n  - include:\n      file: " + filename;
                    Files.write(masterPath, includeEntry.getBytes(), java.nio.file.StandardOpenOption.APPEND);
                }
            } catch (IOException e) {
                logger.error("Failed to update master changelog", e);
            }
        }
    }
    
    private void generateFlywayMigration() {
        String migrationPath = getMigrationPath("flyway");
        createDirectory(migrationPath);
        
        String initSql = "-- Initial Flyway migration\n" +
                        "CREATE TABLE IF NOT EXISTS schema_example (\n" +
                        "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                        "    name VARCHAR(255) NOT NULL,\n" +
                        "    description TEXT,\n" +
                        "    active BOOLEAN DEFAULT TRUE,\n" +
                        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                        "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" +
                        ");";
        
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        writeFile(Paths.get(migrationPath, "V" + timestamp + "__initial_schema.sql"), initSql);
    }
    
    private void generateLiquibaseMigration() {
        String changelogPath = getMigrationPath("liquibase");
        createDirectory(changelogPath);
        
        String masterChangelog = "databaseChangeLog:\n" +
                               "  - changeSet:\n" +
                               "      id: 1\n" +
                               "      author: migration-starter\n" +
                               "      changes:\n" +
                               "        - createTable:\n" +
                               "            tableName: schema_example\n" +
                               "            columns:\n" +
                               "              - column:\n" +
                               "                  name: id\n" +
                               "                  type: bigint\n" +
                               "                  autoIncrement: true\n" +
                               "                  constraints:\n" +
                               "                    primaryKey: true\n" +
                               "                    nullable: false\n" +
                               "              - column:\n" +
                               "                  name: name\n" +
                               "                  type: varchar(255)\n" +
                               "                  constraints:\n" +
                               "                    nullable: false\n" +
                               "              - column:\n" +
                               "                  name: description\n" +
                               "                  type: clob\n" +
                               "              - column:\n" +
                               "                  name: active\n" +
                               "                  type: boolean\n" +
                               "                  defaultValueBoolean: true\n" +
                               "              - column:\n" +
                               "                  name: created_at\n" +
                               "                  type: timestamp\n" +
                               "                  defaultValueComputed: CURRENT_TIMESTAMP\n" +
                               "              - column:\n" +
                               "                  name: updated_at\n" +
                               "                  type: timestamp\n" +
                               "                  defaultValueComputed: CURRENT_TIMESTAMP";

        writeFile(Paths.get(changelogPath, "db.changelog-master.yaml"), masterChangelog);
    }
    
    private String getMigrationPath(String type) {
        if (properties != null && !properties.getLocations().isEmpty()) {
            String location = properties.getLocations().get(0);
            // Convert classpath: to filesystem path
            if (location.startsWith("classpath:")) {
                return "src/main/resources/" + location.substring("classpath:".length());
            } else {
                return location;
            }
        }

        // Default paths if properties not available
        return "flyway".equalsIgnoreCase(type) ? FLYWAY_DEFAULT_PATH : LIQUIBASE_DEFAULT_PATH;
    }

    private void createDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            logger.error("Failed to create directory: {}", path, e);
            throw new RuntimeException("Failed to create directory: " + path, e);
        }
    }
    
    private void writeFile(Path path, String content) {
        try {
            if (!Files.exists(path)) {
                Files.write(path, content.getBytes());
                logger.info("Created file: {}", path);
            } else {
                logger.info("File already exists: {}", path);
            }
        } catch (IOException e) {
            logger.error("Failed to write file: {}", path, e);
            throw new RuntimeException("Failed to write file: " + path, e);
        }
    }
}

