# QualDev Banque - Migration Spring Boot 3

## Présentation

Ce projet correspond à la migration d'une application bancaire vers **Spring Boot 3** (Java 21), avec une base **MySQL** et une interface web, tout en conservant les règles métier de l'ancien projet sous Spring Boot 1.

Objectif principal:
- moderniser l'architecture technique;
- fiabiliser les parcours métier (authentification, comptes, virements);
- faciliter l'exécution et les tests via Docker et Maven.

## Stack technique

- Spring Boot 3.2.x
- Java 21
- Spring MVC / Thymeleaf
- Spring Data JPA
- Spring Security
- MySQL 8
- Maven
- Docker / Docker Compose

## Migration vers Spring Boot 3

La migration apporte notamment:
- un socle applicatif standardisé (configuration, dépendances, packaging);
- une séparation claire entre contrôleurs, services, modèles et repositories;
- une couverture de tests unitaires/intégration renforcée;
- un déploiement simplifié via conteneurs (`app` + `db`).

## Documentation du dossier `doc/`

Les documents principaux sont:

- `doc/01_execution_app.md`  
  Guide pour lancer l'application (Docker et local), exécuter les tests et résoudre les problèmes fréquents.

- `doc/02_repartition_taches.md`  
  Répartition des tâches de l'équipe.


## Démarrage rapide

Depuis la racine:

```bash
docker compose up --build
```

Application disponible sur:
- `http://localhost:8080`

## Tests

Depuis `app/`:

```bash
mvn test
```

