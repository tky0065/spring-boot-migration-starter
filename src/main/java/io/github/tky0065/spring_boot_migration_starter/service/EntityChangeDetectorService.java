package io.github.tky0065.spring_boot_migration_starter.service;

import io.github.tky0065.spring_boot_migration_starter.config.MigrationProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service responsible for detecting changes in JPA entities and generating
 * corresponding migration files.
 */
@Service
public class EntityChangeDetectorService {

    private static final Logger logger = LoggerFactory.getLogger(EntityChangeDetectorService.class);
    private final ApplicationContext applicationContext;
    private final MigrationTemplateGenerator templateGenerator;
    private final MigrationProperties properties;
    private final DataSource dataSource;

    private static final String DEFAULT_MIGRATION_PATH = "src/main/resources/db/migration";
    private static final DateTimeFormatter VERSION_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DESCRIPTION_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public EntityChangeDetectorService(
            ApplicationContext applicationContext,
            MigrationTemplateGenerator templateGenerator,
            MigrationProperties properties,
            DataSource dataSource) {
        this.applicationContext = applicationContext;
        this.templateGenerator = templateGenerator;
        this.properties = properties;
        this.dataSource = dataSource;
    }

    /**
     * Detect entity changes and generate migration files
     *
     * @return true if changes were detected and files were generated
     */
    public boolean detectChangesAndGenerateMigration() {
        if (!properties.isAutoGenerateMigrations()) {
            logger.debug("Auto-generation of migrations is disabled");
            return false;
        }

        logger.info("Detecting entity changes and generating migration files");

        try {
            Set<Class<?>> entityClasses = scanForEntityClasses();
            if (entityClasses.isEmpty()) {
                logger.info("No entity classes found");
                return false;
            }

            logger.info("Found {} entity classes", entityClasses.size());

            // Compare with database schema to detect changes
            Map<String, Set<String>> entityChanges = detectChangesInEntities(entityClasses);
            if (entityChanges.isEmpty()) {
                logger.info("No entity changes detected");
                return false;
            }

            // Generate migration files
            boolean filesGenerated = generateMigrationFiles(entityChanges);

            if (filesGenerated) {
                logger.info("Migration files generated successfully");
            } else {
                logger.info("No migration files were generated");
            }

            return filesGenerated;
        } catch (Exception e) {
            logger.error("Error while detecting entity changes", e);
            return false;
        }
    }

    /**
     * Scan classpath for JPA entity classes
     *
     * @return Set of entity classes
     */
    private Set<Class<?>> scanForEntityClasses() {
        logger.debug("Scanning for entity classes...");
        Set<Class<?>> entityClasses = new HashSet<>();

        try {
            ClassPathScanningCandidateComponentProvider scanner =
                    new ClassPathScanningCandidateComponentProvider(false);

            scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

            // Get base packages from EntityManagerFactory if available
            String[] basePackages = getBasePackages();

            for (String basePackage : basePackages) {
                for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                    try {
                        Class<?> entityClass = Class.forName(bd.getBeanClassName());
                        entityClasses.add(entityClass);
                        logger.debug("Found entity class: {}", entityClass.getName());
                    } catch (ClassNotFoundException e) {
                        logger.warn("Could not load entity class {}", bd.getBeanClassName(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error scanning for entity classes", e);
        }

        return entityClasses;
    }

    /**
     * Get the base packages to scan for entities
     *
     * @return Array of base package names
     */
    private String[] getBasePackages() {
        try {
            LocalContainerEntityManagerFactoryBean emf = applicationContext
                    .getBean(LocalContainerEntityManagerFactoryBean.class);

            if (emf != null && emf.getPersistenceUnitInfo() != null) {
                return new String[] { emf.getPersistenceUnitInfo().getPersistenceUnitName() };
            }
        } catch (Exception e) {
            logger.debug("Could not determine entity scan packages from EntityManagerFactory", e);
        }

        // Default to base package derived from ApplicationContext
        String mainClassName = applicationContext.getBeansWithAnnotation(
                org.springframework.boot.autoconfigure.SpringBootApplication.class)
                .keySet().stream()
                .findFirst()
                .orElse(null);

        if (mainClassName != null) {
            Class<?> mainClass;
            try {
                mainClass = Class.forName(mainClassName);
                return new String[] { mainClass.getPackage().getName() };
            } catch (ClassNotFoundException e) {
                logger.warn("Could not find main application class", e);
            }
        }

        // Ultimate fallback
        return new String[] { "io.github.tky0065" };
    }

    /**
     * Detect changes in entity classes compared to database schema
     *
     * @param entityClasses Set of entity classes to check
     * @return Map of entity names to sets of changed column names
     */
    private Map<String, Set<String>> detectChangesInEntities(Set<Class<?>> entityClasses) {
        Map<String, Set<String>> changes = new HashMap<>();

        // Implementation depends on how you want to compare entities with the database
        // This could use schema comparison tools, JPA metamodel, or Hibernate SchemaExport

        // For now, we'll just add all entities as "changed" for demonstration purposes
        for (Class<?> entityClass : entityClasses) {
            String tableName = getTableName(entityClass);
            changes.put(tableName, new HashSet<>());  // Empty set means the whole table
        }

        return changes;
    }

    /**
     * Get the table name for an entity class
     *
     * @param entityClass The entity class
     * @return The table name
     */
    private String getTableName(Class<?> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null && StringUtils.hasText(tableAnnotation.name())) {
            return tableAnnotation.name();
        }

        // Default to class name
        return entityClass.getSimpleName().toLowerCase();
    }

    /**
     * Generate migration files based on detected changes
     *
     * @param entityChanges Map of entity changes
     * @return true if files were generated
     */
    private boolean generateMigrationFiles(Map<String, Set<String>> entityChanges) {
        if (entityChanges.isEmpty()) {
            return false;
        }

        String migrationsPath = properties.getGeneratedMigrationsPath();
        if (!StringUtils.hasText(migrationsPath)) {
            migrationsPath = DEFAULT_MIGRATION_PATH;
        }

        Path directory = Paths.get(migrationsPath);

        try {
            // Create directories if they don't exist
            Files.createDirectories(directory);

            // Generate appropriate migration files based on the tool type
            if ("flyway".equalsIgnoreCase(properties.getType())) {
                return generateFlywayMigration(directory, entityChanges);
            } else if ("liquibase".equalsIgnoreCase(properties.getType())) {
                return generateLiquibaseMigration(directory, entityChanges);
            } else {
                logger.warn("Unknown migration type: {}", properties.getType());
                return false;
            }
        } catch (IOException e) {
            logger.error("Error creating migration directories", e);
            return false;
        }
    }

    /**
     * Generate Flyway migration SQL files
     *
     * @param directory Base directory for migration files
     * @param entityChanges Map of entity changes
     * @return true if files were generated
     */
    private boolean generateFlywayMigration(Path directory, Map<String, Set<String>> entityChanges) {
        String version = LocalDateTime.now().format(VERSION_FORMATTER);
        String description = "update_schema_" + LocalDateTime.now().format(DESCRIPTION_FORMATTER);
        String filename = "V" + version + "__" + description + ".sql";

        Path filePath = directory.resolve(filename);

        String sqlContent = templateGenerator.generateFlywayMigration(entityChanges);

        try {
            Files.writeString(filePath, sqlContent);
            logger.info("Generated Flyway migration file: {}", filePath);
            return true;
        } catch (IOException e) {
            logger.error("Error writing Flyway migration file", e);
            return false;
        }
    }

    /**
     * Generate Liquibase migration XML files
     *
     * @param directory Base directory for migration files
     * @param entityChanges Map of entity changes
     * @return true if files were generated
     */
    private boolean generateLiquibaseMigration(Path directory, Map<String, Set<String>> entityChanges) {
        String version = LocalDateTime.now().format(VERSION_FORMATTER);
        String filename = "changelog-" + version + ".xml";

        // For Liquibase, we typically need a changelog directory structure
        Path changelogDir = directory.resolve("changelog");
        try {
            Files.createDirectories(changelogDir);
        } catch (IOException e) {
            logger.error("Error creating Liquibase changelog directory", e);
            return false;
        }

        Path filePath = changelogDir.resolve(filename);

        String xmlContent = templateGenerator.generateLiquibaseMigration(entityChanges);

        try {
            Files.writeString(filePath, xmlContent);
            logger.info("Generated Liquibase migration file: {}", filePath);

            // Also update the master changelog if it exists
            updateLiquibaseMasterChangelog(directory, filename);

            return true;
        } catch (IOException e) {
            logger.error("Error writing Liquibase migration file", e);
            return false;
        }
    }

    /**
     * Update the Liquibase master changelog to include the new changelog file
     *
     * @param directory Base directory for migration files
     * @param newChangelogFilename The filename of the new changelog
     */
    private void updateLiquibaseMasterChangelog(Path directory, String newChangelogFilename) {
        Path masterChangelogPath = directory.resolve("db.changelog-master.xml");

        // Create master changelog if it doesn't exist
        if (!Files.exists(masterChangelogPath)) {
            try {
                String masterTemplate = templateGenerator.generateLiquibaseMasterChangelog();
                Files.writeString(masterChangelogPath, masterTemplate);
                logger.info("Created Liquibase master changelog: {}", masterChangelogPath);
            } catch (IOException e) {
                logger.error("Error creating Liquibase master changelog", e);
                return;
            }
        }

        // Add include for the new changelog file
        try {
            String masterContent = new String(Files.readAllBytes(masterChangelogPath));
            String includeEntry = String.format("\t<include file=\"changelog/%s\" relativeToChangelogFile=\"true\"/>",
                    newChangelogFilename);

            // Insert before the closing tag
            String updatedContent = masterContent.replace("</databaseChangeLog>",
                    includeEntry + System.lineSeparator() + "</databaseChangeLog>");

            Files.writeString(masterChangelogPath, updatedContent);
            logger.info("Updated Liquibase master changelog with new include");
        } catch (IOException e) {
            logger.error("Error updating Liquibase master changelog", e);
        }
    }
}
