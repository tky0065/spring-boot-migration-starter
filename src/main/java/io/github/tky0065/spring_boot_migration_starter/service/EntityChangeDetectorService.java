package io.github.tky0065.spring_boot_migration_starter.service;

import io.github.tky0065.spring_boot_migration_starter.config.MigrationProperties;
import jakarta.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
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
public class EntityChangeDetectorService {

    private static final Logger logger = LoggerFactory.getLogger(EntityChangeDetectorService.class);
    private final ApplicationContext applicationContext;
    private final MigrationTemplateGenerator templateGenerator;
    private final MigrationProperties properties;
    private final DataSource dataSource;

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
     */
    public void detectChangesAndGenerateMigration() {
        logger.info("Detecting entity changes and generating migration files");

        try {
            // 1. Get all entity classes
            Set<Class<?>> entityClasses = scanForEntityClasses();

            // 2. Generate schema from entities
            String schemaSQL = generateSchemaFromEntities(entityClasses);

            if (StringUtils.hasText(schemaSQL)) {
                // 3. Create migration file based on the migration type
                if ("flyway".equalsIgnoreCase(properties.getType())) {
                    generateFlywayMigration(schemaSQL);
                } else if ("liquibase".equalsIgnoreCase(properties.getType())) {
                    generateLiquibaseMigration(schemaSQL);
                } else {
                    logger.warn("Unknown migration type: {}. No migrations will be generated.", properties.getType());
                }
            } else {
                logger.info("No schema changes detected or generated");
            }

        } catch (Exception e) {
            logger.error("Error detecting entity changes and generating migrations", e);
        }
    }

    /**
     * Scan classpath for classes annotated with @Entity
     */
    private Set<Class<?>> scanForEntityClasses() {
        Set<Class<?>> entityClasses = new HashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        for (String basePackage : getEntityBasePackages()) {
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                try {
                    Class<?> entityClass = Class.forName(bd.getBeanClassName());
                    entityClasses.add(entityClass);
                } catch (ClassNotFoundException e) {
                    logger.error("Failed to load entity class: {}", bd.getBeanClassName(), e);
                }
            }
        }

        logger.info("Found {} entity classes", entityClasses.size());
        return entityClasses;
    }

    /**
     * Get base packages to scan for entities
     */
    private List<String> getEntityBasePackages() {
        List<String> basePackages = properties.getEntityBasePackages();
        if (basePackages.isEmpty()) {
            // Default to application's base package
            String mainAppClass = properties.getMainApplicationClass();
            if (StringUtils.hasText(mainAppClass)) {
                try {
                    Class<?> appClass = Class.forName(mainAppClass);
                    basePackages.add(appClass.getPackageName());
                } catch (ClassNotFoundException e) {
                    logger.warn("Could not load main application class: {}", mainAppClass, e);
                }
            }

            // If still empty, use a default package
            if (basePackages.isEmpty()) {
                basePackages.add("io.github.tky0065");
            }
        }

        logger.info("Using entity base packages for scanning: {}", basePackages);
        return basePackages;
    }

    /**
     * Generate SQL schema from entity classes using Hibernate SchemaExport
     */
    private String generateSchemaFromEntities(Set<Class<?>> entityClasses) {
        try {
            // Utiliser les propriétés JPA de Spring pour générer le schéma
            Path tempFile = Files.createTempFile("schema-", ".sql");

            Properties props = new Properties();
            props.put("hibernate.dialect", properties.getDialect());
            props.put("hibernate.format_sql", "true");
            props.put("hibernate.hbm2ddl.delimiter", ";");
            props.put("hibernate.hbm2ddl.auto", "create");
            props.put("hibernate.hbm2ddl.scripts.action", "create");
            props.put("hibernate.hbm2ddl.scripts.create-target", tempFile.toString());

            LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
            factoryBean.setDataSource(dataSource);
            factoryBean.setPackagesToScan(getEntityBasePackages().toArray(new String[0]));
            factoryBean.setJpaProperties(props);
            factoryBean.afterPropertiesSet();

            // Lire le contenu du fichier généré
            String schemaSQL = Files.readString(tempFile);
            Files.delete(tempFile);

            return schemaSQL;
        } catch (Exception e) {
            logger.error("Error generating schema from entities", e);
            return "";
        }
    }
    /**
     * Generate Flyway migration file with the schema SQL
     */
    private void generateFlywayMigration(String sql) {
        try {
            String description = "entity_schema_update";
            Path migrationFile = templateGenerator.generateNewFlywayMigration(description, sql);
            if (migrationFile != null) {
                logger.info("Generated Flyway migration file at: {}", migrationFile);
            } else {
                logger.error("Failed to generate Flyway migration file");
            }
        } catch (Exception e) {
            logger.error("Error generating Flyway migration", e);
        }
    }

    /**
     * Generate Liquibase migration file with the schema SQL
     */
    private void generateLiquibaseMigration(String sql) {
        try {
            // Convert SQL to Liquibase XML format
            String changelogXml = convertSqlToLiquibaseXml(sql);

            // Generate the file path
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
            String changelogFileName = timestamp + "_entity_schema_update.xml";

            // Determine the changelog directory
            Path changelogDir = Paths.get(properties.getLiquibaseChangelogDir());
            if (!Files.exists(changelogDir)) {
                Files.createDirectories(changelogDir);
            }

            // Write the changelog file
            Path changelogFile = changelogDir.resolve(changelogFileName);
            Files.writeString(changelogFile, changelogXml);

            // Update the master changelog to include this file
            updateMasterChangelog(changelogFileName);

            logger.info("Generated Liquibase changelog file at: {}", changelogFile);
        } catch (Exception e) {
            logger.error("Error generating Liquibase migration", e);
        }
    }

    /**
     * Convert SQL statements to Liquibase XML format
     */
    private String convertSqlToLiquibaseXml(String sql) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<databaseChangeLog\n");
        xml.append("    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n");
        xml.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("    xmlns:pro=\"http://www.liquibase.org/xml/ns/pro\"\n");
        xml.append("    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n");
        xml.append("    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.25.xsd\n");
        xml.append("    http://www.liquibase.org/xml/ns/pro\n");
        xml.append("    http://www.liquibase.org/xml/ns/pro/liquibase-pro-4.25.xsd\">\n");

        xml.append("    <changeSet id=\"").append(UUID.randomUUID()).append("\" author=\"spring-migration-starter\">\n");

        // Split SQL statements and add them as individual SQL tags
        String[] statements = sql.split(";");
        for (String statement : statements) {
            statement = statement.trim();
            if (!statement.isEmpty()) {
                xml.append("        <sql>").append(statement).append("</sql>\n");
            }
        }

        xml.append("    </changeSet>\n");
        xml.append("</databaseChangeLog>\n");

        return xml.toString();
    }

    /**
     * Update the master changelog to include the new changelog file
     */
    private void updateMasterChangelog(String changelogFileName) throws IOException {
        Path masterChangelogPath = Paths.get(properties.getLiquibaseMasterChangelog());

        // Create master changelog if it doesn't exist
        if (!Files.exists(masterChangelogPath)) {
            String masterChangelogContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<databaseChangeLog\n" +
                    "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
                    "    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.25.xsd\">\n\n" +
                    "</databaseChangeLog>";

            Files.createDirectories(masterChangelogPath.getParent());
            Files.writeString(masterChangelogPath, masterChangelogContent);
        }

        // Read master changelog
        String masterContent = Files.readString(masterChangelogPath);

        // Add include if not already present
        String includeTag = "<include file=\"" + changelogFileName + "\"/>";
        if (!masterContent.contains(includeTag)) {
            // Insert before the closing tag
            int closingTagIndex = masterContent.lastIndexOf("</databaseChangeLog>");
            if (closingTagIndex > 0) {
                StringBuilder updatedContent = new StringBuilder(masterContent);
                updatedContent.insert(closingTagIndex, "    " + includeTag + "\n\n    ");
                Files.writeString(masterChangelogPath, updatedContent.toString());
            }
        }
    }
}
