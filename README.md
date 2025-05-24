# Spring Boot Migration Starter

Un starter Spring Boot qui simplifie la configuration et l'utilisation des outils de migration de base de données. Ce starter prend en charge à la fois Flyway et Liquibase, permettant aux développeurs de choisir facilement leur solution préférée via une configuration simple.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.tky0065/spring-boot-migration-starter.svg)](https://central.sonatype.com/artifact/io.github.tky0065/spring-boot-migration-starter)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

## Caractéristiques

- Configuration automatique pour Flyway et Liquibase
- Possibilité de basculer facilement entre Flyway et Liquibase
- Génération automatique des templates de migration initiaux
- Configuration personnalisable via des propriétés simples
- Intégration transparente avec Spring Boot

## Prérequis

- Java 21 ou supérieur
- Spring Boot 3.x

## Installation

Ajoutez la dépendance à votre projet Maven :

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>spring-boot-migration-starter</artifactId>
    <version>0.0.3</version>
</dependency>
```

Ou pour Gradle :

```gradle
implementation 'io.github.tky0065:spring-boot-migration-starter:0.0.3'
```

## Configuration

Le starter offre plusieurs options configurables dans votre fichier `application.properties` ou `application.yml`.

### Configuration YAML

```yaml
db:
  migration:
    type: flyway  # ou "liquibase"
    enabled: true
    locations:
      - classpath:db/migration
      - classpath:db/changelog
    baseline-on-migrate: true
    validate-on-migrate: true
    clean-disabled: true
    change-log-path: classpath:db/changelog/db.changelog-master.xml  # Pour Liquibase uniquement
```

### Configuration Properties

```properties
db.migration.type=flyway
db.migration.enabled=true
db.migration.locations=classpath:db/migration
db.migration.baseline-on-migrate=true
db.migration.validate-on-migrate=true
db.migration.clean-disabled=true
db.migration.change-log-path=classpath:db/changelog/db.changelog-master.xml
```

## Options de Configuration

| Propriété | Description | Valeur par défaut |
|-----------|-------------|-------------------|
| `db.migration.type` | Type d'outil de migration (flyway ou liquibase) | `flyway` |
| `db.migration.enabled` | Active ou désactive les migrations | `true` |
| `db.migration.locations` | Chemin vers les scripts de migration | - |
| `db.migration.baseline-on-migrate` | Pour Flyway : Initialiser la ligne de base si nécessaire | `true` |
| `db.migration.validate-on-migrate` | Pour Flyway : Valider les migrations avant exécution | `true` |
| `db.migration.clean-disabled` | Pour Flyway : Désactiver la commande clean | `true` |
| `db.migration.change-log-path` | Pour Liquibase : Chemin vers le fichier changelog principal | - |

## Utilisation

### Avec Flyway (par défaut)

1. Ajoutez le starter à votre projet
2. Créez vos scripts de migration dans `src/main/resources/db/migration`
3. Les scripts seront automatiquement exécutés au démarrage de l'application

Exemple de script Flyway :
```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Avec Liquibase

1. Ajoutez le starter à votre projet
2. Configurez `db.migration.type=liquibase`
3. Créez vos changelogs dans `src/main/resources/db/changelog`
4. Les migrations seront automatiquement exécutées au démarrage de l'application

Exemple de changelog Liquibase :
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.25.xsd">

    <changeSet id="1" author="enokdev">
        <createTable tableName="users">
            <column name="id" type="bigint" autoIncrement="true">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="username" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
            <column name="email" type="varchar(255)">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

## Services disponibles

Le starter fournit ces services injectables :

### MigrationService

Interface commune pour les opérations de migration :

```java
@Autowired
private MigrationService migrationService;

// Exécuter les migrations
migrationService.migrate();

// Valider les migrations
migrationService.validate();

// Réparer les métadonnées de migration (si supporté)
migrationService.repair();
```

## Avantages par rapport à la configuration manuelle

- Configuration automatique basée sur des propriétés simples
- Bascule facile entre Flyway et Liquibase sans changer le code
- Génération de templates de migration à l'initialisation
- Paramètres pré-configurés avec des valeurs recommandées

## Contribuer

Les contributions sont les bienvenues ! N'hésitez pas à ouvrir une issue ou un pull request sur GitHub.

1. Fork le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/amazing-feature`)
3. Committez vos changements (`git commit -m 'Add some amazing feature'`)
4. Push sur la branche (`git push origin feature/amazing-feature`)
5. Ouvrez un Pull Request

## License

Ce projet est distribué sous la licence MIT. Voir le fichier `LICENSE` pour plus d'informations.

## Auteurs

- [enokdev](https://enok-dev.vercel.app/) - Créateur et mainteneur principal
