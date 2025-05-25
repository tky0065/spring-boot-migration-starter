package io.github.tky0065.spring_boot_migration_starter.service;

import io.github.tky0065.spring_boot_migration_starter.config.MigrationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

/**
 * Service for generating migration script templates
 */
@Service
public class MigrationTemplateGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(MigrationTemplateGenerator.class);
    private static final String FLYWAY_DEFAULT_PATH = "src/main/resources/db/migration";
    private static final String LIQUIBASE_DEFAULT_PATH = "src/main/resources/db/changelog";

    private final MigrationProperties properties;

    public MigrationTemplateGenerator(ResourceLoader resourceLoader, MigrationProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Generate initial migration templates based on the specified type
     *
     * @param type Migration tool type (flyway or liquibase)
     */
    public void generateInitialMigrations(String type) {
        logger.info("Generating initial migration templates for type: {}", type);
        if ("flyway".equalsIgnoreCase(type)) {
            generateFlywayInitialMigration();
        } else if ("liquibase".equalsIgnoreCase(type)) {
            generateLiquibaseInitialMigration();
        } else {
            logger.warn("Unknown migration type: {}. No templates will be generated.", type);
        }
    }

    /**
     * Generate a new Flyway migration file with the provided SQL content
     *
     * @param description Description to include in the filename
     * @param sql SQL content to write
     * @return Path to the generated file or null if generation failed
     */
    public Path generateNewFlywayMigration(String description, String sql) {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String filename = String.format("V%s__%s.sql", timestamp, description.replace(' ', '_'));

        String directory = StringUtils.hasText(properties.getGeneratedMigrationsPath()) ?
                properties.getGeneratedMigrationsPath() : FLYWAY_DEFAULT_PATH;

        Path path = Paths.get(directory, filename);

        try {
            // Create directory if it doesn't exist
            Files.createDirectories(path.getParent());

            // Write the SQL content to the file
            Files.writeString(path, sql);
            logger.info("Generated Flyway migration: {}", path);
            return path;
        } catch (IOException e) {
            logger.error("Failed to create Flyway migration file", e);
            return null;
        }
    }
    
    /**
     * Generate a Flyway migration script based on entity changes
     *
     * @param entityChanges Map of entity names to sets of changed column names
     * @return The SQL content for the migration
     */
    public String generateFlywayMigration(Map<String, Set<String>> entityChanges) {
        StringBuilder sql = new StringBuilder();
        sql.append("-- Migration generated automatically by spring-boot-migration-starter\n");
        sql.append("-- Generated on ").append(LocalDateTime.now()).append("\n\n");

        // For demonstration purposes, we'll just create basic table creation statements
        for (Map.Entry<String, Set<String>> entry : entityChanges.entrySet()) {
            String tableName = entry.getKey();
            sql.append("-- Table: ").append(tableName).append("\n");

            // In a real implementation, this would inspect the entity class
            // and generate appropriate DDL statements.
            sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");
            sql.append("    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n");
            sql.append("    name VARCHAR(255),\n");
            sql.append("    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n");
            sql.append(");\n\n");
        }

        return sql.toString();
    }

    /**
     * Generate the initial Flyway migration file with a basic schema
     *
     * @return Path to the generated file or null if generation failed
     */
    public Path generateFlywayInitialMigration() {
        String sql = "-- Initial schema setup\n\n" +
                "-- You can put your initial schema setup here\n" +
                "-- For example, creating basic tables, sequences, etc.\n\n" +
                "-- Example:\n" +
                "-- CREATE TABLE example (\n" +
                "--   id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                "--   name VARCHAR(255) NOT NULL,\n" +
                "--   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
                "-- );\n";

        return generateNewFlywayMigration("initial_schema", sql);
    }

    /**
     * Generate a Liquibase migration script based on entity changes
     *
     * @param entityChanges Map of entity names to sets of changed column names
     * @return The XML content for the migration
     */
    public String generateLiquibaseMigration(Map<String, Set<String>> entityChanges) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<databaseChangeLog\n");
        xml.append("        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n");
        xml.append("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n");
        xml.append("         https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.5.xsd\">\n\n");

        String changesetId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        xml.append("    <changeSet id=\"").append(changesetId).append("\" author=\"spring-boot-migration-starter\">\n");

        // For demonstration purposes, we'll just create basic table creation statements
        for (Map.Entry<String, Set<String>> entry : entityChanges.entrySet()) {
            String tableName = entry.getKey();

            xml.append("        <!-- Table: ").append(tableName).append(" -->\n");
            xml.append("        <createTable tableName=\"").append(tableName).append("\">\n");
            xml.append("            <column name=\"id\" type=\"BIGINT\" autoIncrement=\"true\">\n");
            xml.append("                <constraints primaryKey=\"true\" nullable=\"false\"/>\n");
            xml.append("            </column>\n");
            xml.append("            <column name=\"name\" type=\"VARCHAR(255)\"/>\n");
            xml.append("            <column name=\"created_at\" type=\"TIMESTAMP\" defaultValueComputed=\"CURRENT_TIMESTAMP\"/>\n");
            xml.append("        </createTable>\n\n");
        }

        xml.append("    </changeSet>\n");
        xml.append("</databaseChangeLog>");

        return xml.toString();
    }

    /**
     * Generate the initial Liquibase setup with master changelog and an initial changeset
     *
     * @return Path to the generated master changelog file or null if generation failed
     */
    public Path generateLiquibaseInitialMigration() {
        try {
            // Determine the directory to use
            String directory = StringUtils.hasText(properties.getGeneratedMigrationsPath()) ?
                    properties.getGeneratedMigrationsPath() : LIQUIBASE_DEFAULT_PATH;

            Path dirPath = Paths.get(directory);
            Files.createDirectories(dirPath);

            // Create the changelog directory if it doesn't exist
            Path changelogDir = dirPath.resolve("changelog");
            Files.createDirectories(changelogDir);

            // Generate the initial changeset
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
            String initialChangelogFile = "changelog-" + timestamp + ".xml";
            Path initialChangelogPath = changelogDir.resolve(initialChangelogFile);

            String initialChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<databaseChangeLog\n" +
                    "        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
                    "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
                    "         https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.5.xsd\">\n\n" +
                    "    <changeSet id=\"" + timestamp + "\" author=\"spring-boot-migration-starter\">\n" +
                    "        <!-- Initial schema setup -->\n" +
                    "        <!-- Example: -->\n" +
                    "        <!-- <createTable tableName=\"example\"> -->\n" +
                    "        <!--     <column name=\"id\" type=\"BIGINT\" autoIncrement=\"true\"> -->\n" +
                    "        <!--         <constraints primaryKey=\"true\" nullable=\"false\"/> -->\n" +
                    "        <!--     </column> -->\n" +
                    "        <!--     <column name=\"name\" type=\"VARCHAR(255)\"/> -->\n" +
                    "        <!--     <column name=\"created_at\" type=\"TIMESTAMP\" defaultValueComputed=\"CURRENT_TIMESTAMP\"/> -->\n" +
                    "        <!-- </createTable> -->\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>";

            Files.writeString(initialChangelogPath, initialChangelog);
            logger.info("Generated Liquibase initial changelog: {}", initialChangelogPath);

            // Create the master changelog file
            Path masterChangelogPath = dirPath.resolve("db.changelog-master.xml");
            String masterChangelog = generateLiquibaseMasterChangelog();

            Files.writeString(masterChangelogPath, masterChangelog);
            logger.info("Generated Liquibase master changelog: {}", masterChangelogPath);

            // Update the master changelog to include the initial changelog
            String updatedMasterChangelog = masterChangelog.replace("</databaseChangeLog>",
                    "\t<include file=\"changelog/" + initialChangelogFile + "\" relativeToChangelogFile=\"true\"/>\n</databaseChangeLog>");

            Files.writeString(masterChangelogPath, updatedMasterChangelog);

            return masterChangelogPath;
        } catch (IOException e) {
            logger.error("Failed to create Liquibase migration files", e);
            return null;
        }
    }

    /**
     * Generate a Liquibase master changelog XML file
     *
     * @return String representation of the master changelog XML
     */
    public String generateLiquibaseMasterChangelog() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<databaseChangeLog\n" +
                "        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
                "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
                "         https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.5.xsd\">\n" +
                "    <!-- Include additional changelog files here -->\n" +
                "</databaseChangeLog>";
    }
}
