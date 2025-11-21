# Étape 1 : build du WAR avec Maven + JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 1) Copier le pom et télécharger les dépendances (cache Docker)
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# 2) Copier les sources
COPY src ./src
COPY WebContent ./WebContent

# 3) Builder le WAR (sans lancer les tests)
RUN mvn -B -ntp clean package

# Étape 2 : image Tomcat avec JDK 17
FROM tomcat:9.0-jdk17-temurin

# Déployer le WAR généré en tant que ROOT.war
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Tomcat écoute en interne sur 8080
EXPOSE 8088

CMD ["catalina.sh", "run"]
