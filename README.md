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
- Support pour les dialectes spécifiques de bases de données (MySQL, PostgreSQL)
- Détection automatique des changements d'entités JPA
- Génération automatique des scripts de migration
- Logging détaillé des opérations de migration

## Prérequis

- Java 21 ou supérieur
- Spring Boot 3.x

## Installation

Ajoutez la dépendance à votre projet Maven :

```xml
<dependency>
    <groupId>io.github.tky0065</groupId>
    <artifactId>spring-boot-migration-starter</artifactId>
    <version>0.0.6</version>
</dependency>
```

Ou pour Gradle :

```gradle
implementation 'io.github.tky0065:spring-boot-migration-starter:0.0.6'
```

## Configuration

Le starter offre plusieurs options configurables dans votre fichier `application.properties` ou `application.yml`.

### Configuration YAML

```yaml
db:
  migration:
    # Type d'outil de migration (flyway ou liquibase)
    type: flyway
    
    # Active ou désactive les migrations
    enabled: true
    
    # Chemins des scripts de migration (liste)
    locations:
      - classpath:db/migration
      - filesystem:/path/to/migrations
      
    # Chemin unique des scripts de migration (pour compatibilité)
    location: classpath:db/migration
    
    # Options spécifiques à Flyway
    baseline-on-migrate: true
    validate-on-migrate: true
    clean-disabled: true
    baseline-version: "1"
    schema: public
    
    # Options spécifiques à Liquibase
    change-log-path: db/changelog/db.changelog-master.yaml
    contexts: dev,test
    labels: version-1.0
    
    # Activation des identifiants SQL entre guillemets (utile pour les mots-clés réservés)
    quote-identifiers: false
    
    # Configuration de la génération automatique des migrations
    auto-generate-migrations: false
    generated-migrations-path: src/main/resources/db/migration
    
    # Propriétés supplémentaires pour Flyway
    flyway-properties:
      flyway.outOfOrder: true
      flyway.ignoreMissingMigrations: true
      
    # Propriétés supplémentaires pour Liquibase
    liquibase-properties:
      liquibase.dropFirst: false
      liquibase.changeLogLockWaitTimeInMinutes: 5
```

### Configuration Properties

```properties
# Type d'outil de migration
db.migration.type=flyway

# Active ou désactive les migrations
db.migration.enabled=true

# Chemins des scripts de migration
db.migration.locations[0]=classpath:db/migration
db.migration.locations[1]=filesystem:/path/to/migrations

# Chemin unique des scripts de migration (pour compatibilité)
db.migration.location=classpath:db/migration

# Options Flyway
db.migration.baseline-on-migrate=true
db.migration.validate-on-migrate=true
db.migration.clean-disabled=true
db.migration.schema=public
db.migration.baseline-version=1

# Options Liquibase
db.migration.change-log-path=db/changelog/db.changelog-master.yaml
db.migration.contexts=dev,test
db.migration.labels=version-1.0

# Activation des identifiants SQL entre guillemets
db.migration.quote-identifiers=false

# Génération automatique des migrations
db.migration.auto-generate-migrations=false
db.migration.generated-migrations-path=src/main/resources/db/migration

# Propriétés supplémentaires
db.migration.flyway-properties.flyway.outOfOrder=true
db.migration.liquibase-properties.liquibase.dropFirst=false
```

## Utilisation

### Configuration simple

Le starter est automatiquement configuré et activé dès que vous l'ajoutez en tant que dépendance. Par défaut, il utilisera Flyway comme outil de migration avec le chemin standard `classpath:db/migration`.

### Utilisation de Liquibase

Pour utiliser Liquibase au lieu de Flyway, modifiez la configuration :

```yaml
db:
  migration:
    type: liquibase
    change-log-path: db/changelog/db.changelog-master.yaml
```

### Génération automatique des scripts de migration

Pour activer la génération automatique des scripts de migration basée sur les changements d'entités JPA :

```yaml
db:
  migration:
    auto-generate-migrations: true
    generated-migrations-path: src/main/resources/db/migration
```

Cette fonctionnalité analysera vos entités JPA au démarrage de l'application et générera des scripts de migration si des changements sont détectés.

### Support des bases de données spécifiques

Le starter inclut désormais un support pour les dialectes spécifiques de bases de données :

- MySQL : Supporte les fonctionnalités spécifiques à MySQL avec Flyway
- PostgreSQL : Supporte les fonctionnalités spécifiques à PostgreSQL avec Flyway

Il n'est pas nécessaire de configurer ces dialectes, ils sont automatiquement détectés.

### Utilisation programmatique

Vous pouvez également utiliser les services de migration de manière programmatique :

```java
@Autowired
private MigrationService migrationService;

// Pour exécuter une migration manuellement
public void performMigration() {
    migrationService.migrate();
}

// Pour valider le schéma
public void validateSchema() {
    migrationService.validate();
}

// Pour réparer les migrations (si supporté)
public void repairMigrations() {
    migrationService.repair();
}
```

### Génération de modèles de migration

Pour générer des templates de migration :

```java
@Autowired
private MigrationTemplateGenerator templateGenerator;

// Générer un nouveau script Flyway
public void generateFlyway() {
    templateGenerator.generateFlywayInitialMigration();
}

// Générer un nouveau changelog Liquibase
public void generateLiquibase() {
    templateGenerator.generateLiquibaseInitialMigration();
}
```

## Exemples concrets

### Exemple avec Flyway et MySQL

```yaml
db:
  migration:
    type: flyway
    locations:
      - classpath:db/migration/common
      - classpath:db/migration/mysql
    schema: myapp
    baseline-on-migrate: true
    quote-identifiers: true
    flyway-properties:
      flyway.placeholderReplacement: true
      flyway.placeholders.tablespace: my_tablespace
```

### Exemple avec Liquibase et plusieurs contextes

```yaml
db:
  migration:
    type: liquibase
    change-log-path: db/changelog/db.changelog-master.xml
    contexts: dev,test
    labels: version-2.0
```

## Support et contribution

Les contributions sont les bienvenues ! Si vous rencontrez des problèmes ou avez des suggestions d'amélioration, n'hésitez pas à :

1. Ouvrir une issue sur GitHub
2. Soumettre une Pull Request avec vos propositions de modification

## Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.
