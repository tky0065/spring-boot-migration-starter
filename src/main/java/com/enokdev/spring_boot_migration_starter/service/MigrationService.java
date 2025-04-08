package com.enokdev.spring_boot_migration_starter.service;

public interface MigrationService {
    void migrate();
    void validate();
    void repair();
}