package io.github.tky0065.spring_boot_migration_starter;

import io.github.tky0065.spring_boot_migration_starter.config.MigrationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MigrationProperties.class)
public class SpringBootMigrationStarterApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootMigrationStarterApplication.class, args);
	}

}
