package io.github.tky0065.spring_boot_migration_starter;

import io.github.tky0065.spring_boot_migration_starter.config.MigrationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(MigrationProperties.class)
class SpringBootMigrationStarterApplicationTests {

    @Test
    void contextLoads() {
    }

}
