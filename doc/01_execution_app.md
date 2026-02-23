# Exécuter l'application

## 1) Prérequis

- Docker + Docker Compose
- Java 21 (si exécution locale sans Docker)
- Maven 3.9+ (si exécution locale sans Docker)

## 2) Lancement recommandé (Docker)

Depuis la racine du projet:

```bash
docker compose up --build
```

Services démarrés:

- Application Spring Boot: `http://localhost:8080`
- Base MySQL: `localhost:3307`

## 3) Arrêt des services

```bash
docker compose down
```

Supprimer aussi les volumes (reset DB complet):

```bash
docker compose down -v
```

## 4) Exécution locale (sans Docker)

### 4.1 Démarrer MySQL

Option A: via Docker uniquement pour la DB

```bash
docker compose up db
```

### 4.2 Lancer l'application

Depuis `app/`:

```bash
mvn spring-boot:run
```


## 5) Données initiales

Les scripts SQL d'initialisation sont dans:

- `db/init/`

Ils sont chargés automatiquement au premier démarrage du conteneur MySQL.

## 6) Lancer les tests

Depuis `app/`:

```bash
mvn test
```

Tests d'intégration (failsafe, `*IT.java`):

```bash
mvn verify
```

## 7) Accès fonctionnel

- Écran de login: `http://localhost:8080/login`
- Comptes de démonstration affichés dans la page de login.

## 8) Problèmes fréquents

### Port déjà occupé (8080 ou 3307)

- Modifier les ports dans `docker-compose.yml`
- Ou arrêter le process qui utilise déjà le port

### "Table/Schema not found"

- Faire un reset complet:

```bash
docker compose down -v
docker compose up --build
```

### Erreur de connexion DB en local

Vérifier:

- MySQL bien démarré
- Port `3307` accessible
- Variables `DB_*` cohérentes
