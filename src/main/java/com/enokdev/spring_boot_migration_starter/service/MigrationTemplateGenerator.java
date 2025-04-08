package com.enokdev.spring_boot_migration_starter.service;

import org.springframework.core.io.ResourceLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MigrationTemplateGenerator {
    
    private final ResourceLoader resourceLoader;
    
    public MigrationTemplateGenerator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    public void generateInitialMigrations(String type) {
        if ("flyway".equalsIgnoreCase(type)) {
            generateFlywayMigration();
        } else if ("liquibase".equalsIgnoreCase(type)) {
            generateLiquibaseMigration();
        }
    }
    
    private void generateFlywayMigration() {
        String migrationPath = "src/main/resources/db/migration";
        createDirectory(migrationPath);
        
        String initSql = "-- Initial Flyway migration\n" +
                        "CREATE TABLE IF NOT EXISTS example (\n" +
                        "    id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                        "    name VARCHAR(255) NOT NULL,\n" +
                        "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
                        ");";
        
        writeFile(Paths.get(migrationPath, "V1__init.sql"), initSql);
    }
    
    private void generateLiquibaseMigration() {
        String changelogPath = "src/main/resources/db/changelog";
        createDirectory(changelogPath);
        
        String masterChangelog = "databaseChangeLog:\n" +
                               "  - changeSet:\n" +
                               "      id: 1\n" +
                               "      author: migration-starter\n" +
                               "      changes:\n" +
                               "        - createTable:\n" +
                               "            tableName: example\n" +
                               "            columns:\n" +
                               "              - column:\n" +
                               "                  name: id\n" +
                               "                  type: bigint\n" +
                               "                  autoIncrement: true\n" +
                               "                  constraints:\n" +
                               "                    primaryKey: true\n" +
                               "                    nullable: false";
        
        writeFile(Paths.get(changelogPath, "db.changelog-master.yaml"), masterChangelog);
    }
    
    private void createDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + path, e);
        }
    }
    
    private void writeFile(Path path, String content) {
        try {
            if (!Files.exists(path)) {
                Files.write(path, content.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + path, e);
        }
    }
}