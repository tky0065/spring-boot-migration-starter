package io.github.tky0065.spring_boot_migration_starter.service;

public interface MigrationService {
    void migrate();
    void validate();
    void repair();
}