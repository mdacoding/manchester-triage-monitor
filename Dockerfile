# ── Stage 1: Build ────────────────────────────────────────────────────────
# Nutzt ein offizielles Maven-Image nur zum Bauen, damit das finale Image
# keine Build-Tools enthaelt (kleineres Image, kleinere Angriffsflaeche).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Abhaengigkeiten zuerst kopieren, um den Docker-Layer-Cache zu nutzen
# (Aenderungen am Code invalidieren dann nicht den Dependency-Download).
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q package -DskipTests

# ── Stage 2: Runtime ─────────────────────────────────────────────────────
# Schlankes JRE-Image (kein volles JDK) fuer den produktiven Betrieb.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Als Non-Root-User laufen lassen (Sicherheits-Best-Practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
