# 🏥 Triage Dashboard

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker)](https://www.docker.com/)

**Echtzeit-Triage- und Notaufnahme-Dashboard** basierend auf dem Manchester-Triage-System (MTS). Eine Full-Stack-Anwendung zur digitalen Verwaltung und Visualisierung von Patientenwartelisten in Krankenhaus-Notaufnahmen.

---

## ✨ Features

### Klinische Funktionalität
- 🚨 **MTS-konforme Triage**: 5 Dringlichkeitsstufen (RED, ORANGE, YELLOW, GREEN, BLUE)
- ⚡ **Echtzeit-Updates**: WebSocket-basierte Push-Benachrichtigungen für alle verbundenen Clients
- 📊 **Automatische Priorisierung**: Sortierung nach Dringlichkeit und Ankunftszeit (FIFO)
- 🔄 **Re-Triage**: Dynamische Aktualisierung der Triagestufe mit automatischer Neupriorisierung
- ⏱️ **Wartezeitberechnung**: Klinisch definierte Maximalwartezeiten pro Stufe
- 📦 **Archivierung**: Abschluss von Patientenfallen mit Audit-Trail

### Technische Highlights
- 🎨 **Modernes UI**: React 19 + Tailwind CSS mit klinisch-beruhigendem Design
- 🔌 **WebSocket-Kommunikation**: STOMP-over-WebSocket mit automatischem Reconnect
- 🗄️ **Datenbank**: PostgreSQL mit Flyway-Migrationen
- 📝 **API-Dokumentation**: Interaktive Swagger UI
- ✅ **Validierung**: Bean Validation mit aussagekräftigen Fehlermeldungen
- 🧪 **Testbereit**: H2 In-Memory-Datenbank für Integrationstests

---

## 🏗️ Architektur

```
triage-dashboard/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/de/hospital/triagedashboard/
│   │   ├── config/            # WebSocket-Konfiguration
│   │   ├── controller/        # REST-Controller + Exception Handling
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── mapper/            # MapStruct DTO-Mapper
│   │   ├── model/             # JPA Entities + Enums
│   │   ├── repository/        # Spring Data JPA Repositories
│   │   └── service/           # Geschäftslogik
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/      # Flyway SQL-Migrationen
│
└── frontend/                   # React Frontend
    └── src/
        ├── components/        # Wiederverwendbare UI-Komponenten
        ├── hooks/             # Custom Hooks (WebSocket)
        ├── pages/             # Seiten-Komponenten
        ├── config/            # Triage-Konfiguration
        └── utils/             # Hilfsfunktionen
```

### Technologie-Stack

**Backend:**
- Java 21
- Spring Boot 3.3.0
- Spring WebSocket (STOMP Broker)
- Spring Data JPA
- PostgreSQL 16
- Flyway (Datenbankmigrationen)
- MapStruct (DTO-Mapping)
- Lombok (Boilerplate-Reduktion)
- SpringDoc OpenAPI (Swagger UI)

**Frontend:**
- React 19
- Vite 8
- Tailwind CSS 3.4
- @stomp/stompjs + SockJS
- ESLint

---

## 🚀 Quick Start

### Voraussetzungen

- Docker & Docker Compose
- Node.js 18+ und npm
- Java 21
- Maven 3.8+ (oder Maven Wrapper)

### Installation

#### 1. Repository klonen

```bash
git clone https://github.com/your-username/triage-dashboard.git
cd triage-dashboard
```

#### 2. Datenbank starten

```bash
docker-compose up -d
```

Dies startet PostgreSQL auf Port 5432 mit:
- **User**: `triage_user`
- **Password**: `triage_password`
- **Datenbank**: `triage_db`

#### 3. Backend starten

```bash
# Mit Maven Wrapper
./mvnw spring-boot:run

# Oder mit installiertem Maven
mvn spring-boot:run
```

Das Backend läuft auf `http://localhost:8080`

**Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

#### 4. Frontend starten

```bash
cd frontend
npm install
npm run dev
```

Das Frontend läuft auf `http://localhost:5173`

---

## 📖 Nutzung

### Patienten aufnehmen

1. Klicke auf **"+ Neuer Patient"** in der Kopfzeile
2. Fülle das Formular aus:
   - Name des Patienten
   - Triagestufe (MTS)
   - Symptome / Ersteinschätzung
3. Klicke auf **"Patient aufnehmen"**

### Triagestufe aktualisieren (Re-Triage)

Die aktuelle Implementierung zeigt die Funktionalität auf Backend-Ebene. Eine UI-Komponente für Re-Triage kann leicht über den REST-Endpoint `PUT /api/triage/patient/{id}/level` ergänzt werden.

### Patient archivieren

Klicke auf **"Entlassen"** in der Patientenkarte, um einen Fall abzuschließen. Archivierte Fälle werden aus der Warteliste entfernt, bleiben aber in der Datenbank erhalten.

---

## 🔌 API-Endpoints

### REST API

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| `GET` | `/api/triage/queue` | Aktuelle Warteliste (sortiert) |
| `POST` | `/api/triage/patient` | Neuen Patienten aufnehmen |
| `PUT` | `/api/triage/patient/{id}/level` | Triagestufe aktualisieren |
| `PATCH` | `/api/triage/patient/{id}/archive` | Patientenfall archivieren |

### WebSocket

| Endpoint | Beschreibung |
|----------|--------------|
| `ws://localhost:8080/ws-triage` | STOMP WebSocket-Endpoint |
| `/topic/queue` | Broadcast-Topic für Wartelisten-Updates |

---

## 🧪 Tests

### Backend-Tests

```bash
./mvnw test
```

### Frontend-Linting

```bash
cd frontend
npm run lint
```

---

## 🐳 Docker

### Datenbank (bereits konfiguriert)

```bash
docker-compose up -d
docker-compose down
```

### Backend containerisieren (TODO)

```dockerfile
# Multi-Stage Build für Production
```

---

## 📋 Manchester-Triage-System (MTS)

Das Dashboard implementiert das standardisierte MTS mit 5 Dringlichkeitsstufen:

| Stufe | Label | Maximale Wartezeit | Farbe |
|-------|-------|-------------------|-------|
| **RED** | Sofort | 0 Minuten | 🔴 Rot |
| **ORANGE** | Sehr dringend | 10 Minuten | 🟠 Orange |
| **YELLOW** | Dringend | 30 Minuten | 🟡 Gelb |
| **GREEN** | Normal | 90 Minuten | 🟢 Grün |
| **BLUE** | Nicht dringend | 120 Minuten | 🔵 Blau |

---

## 🛠️ Entwicklung

### Projektstruktur

Das Backend folgt einer **klaren Schichtenarchitektur**:

- **Controller**: REST-Endpoints + WebSocket-Broadcasts
- **Service**: Geschäftslogik (Triage-Logik, Wartezeitberechnung)
- **Repository**: Datenzugriff (Spring Data JPA)
- **DTO**: API-Verträge (Request/Response)
- **Mapper**: DTO ↔ Entity Konvertierung (MapStruct)

### Code-Qualität

- ✅ Lombok für boilerplate-freien Code
- ✅ MapStruct für typsicheres DTO-Mapping
- ✅ Bean Validation für Input-Validierung
- ✅ Globale Exception-Handling mit RFC 9457 ProblemDetail
- ✅ Umfassende JavaDoc-Dokumentation
- ✅ Strukturiertes Logging mit SLF4J

---

## 📝 Lizenz

Dieses Projekt wurde im Rahmen einer Studienarbeit / als Demo-Projekt entwickelt.

---

## 🤝 Beitrag leisten

Beiträge sind willkommen! Bitte beachte:

1. Fork das Repository
2. Erstelle einen Feature-Branch (`git checkout -b feature/AmazingFeature`)
3. Commit deine Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. Push zum Branch (`git push origin feature/AmazingFeature`)
5. Öffne einen Pull Request

---

## 📞 Kontakt

Bei Fragen oder Anregungen zum Projekt:

- 📧 E-Mail: deine-email@example.com
- 🐛 Issues: [GitHub Issues](https://github.com/your-username/triage-dashboard/issues)

---

## 🙏 Danksagungen

- **Manchester-Triage-System**: Standardisiertes Triage-System für Notaufnahmen
- **Spring Boot Team**: Exzellentes Framework für Enterprise-Anwendungen
- **React Team**: Moderne UI-Bibliothek
- **Tailwind CSS**: Utility-first CSS Framework

---

## 📚 Weiterführende Links

- [Manchester Triage System](https://www.manchestertriage.org/)
- [Spring Boot Dokumentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React Dokumentation](https://react.dev/)
- [Tailwind CSS](https://tailwindcss.com/)
- [STOMP Protocol](https://stomp.github.io/)

---

**⚠️ Hinweis**: Dies ist eine Demo-Anwendung. Für den Produktivbetrieb müssen zusätzlich Security, umfassende Tests und weitere Compliance-Maßnahmen implementiert werden.