# Portfolio-Texte: Manchester Triage Dashboard

Drei Varianten für unterschiedliche Kontexte – LinkedIn-Post, Kurzbeschreibung (Portfolio-Website/CV) und ausführliche Case Study. Alle nutzen dieselben Fakten, unterscheiden sich nur in Länge/Ton.

---

## 1. LinkedIn-Post (kurz, storyorientiert)

🏥 **Neues Portfolio-Projekt: Manchester Triage Dashboard**

Ein Echtzeit-Dashboard für Notaufnahmen, das Patient:innen nach dem Manchester Triage System (MTS) automatisch nach klinischer Dringlichkeit priorisiert – live synchronisiert über alle angemeldeten Arbeitsplätze per WebSocket.

Der interessanteste Teil war nicht das Feature-Bauen, sondern ein Bug, den ich beim finalen Smoke-Test entdeckt habe: Die Warteliste sortierte **alphabetisch statt klinisch** (GRÜN vor ROT) – ein Klassiker, wenn ein Enum als String in der DB liegt und die Datenbank selbst sortiert. In einer echten Notaufnahme wäre das ein Patientensicherheitsrisiko gewesen. Fix: Sortierlogik explizit in die Service-Schicht verschoben (Java-Comparator statt SQL `ORDER BY`) und mit einem Regressionstest abgesichert.

**Tech-Stack:** Java 21 / Spring Boot 3 (WebSocket/STOMP, Spring Security, JWT, JPA, Flyway) im Backend, React 19 / Vite / Tailwind im Frontend, PostgreSQL, Docker, GitHub Actions CI.

Komplett kostenlos deployed (Vercel + Render + Neon, Free Tier) – Code, Tests, CI und Doku öffentlich auf GitHub.

🔗 Live-Demo: https://manchester-triage-monitor.vercel.app
🔗 Code: https://github.com/mdacoding/manchester-triage-monitor

#SoftwareEngineering #SpringBoot #React #FullStack #HealthTech

---

## 2. Kurzbeschreibung (für Portfolio-Website / CV-Projektliste)

**Manchester Triage Dashboard** — Full-Stack-Webanwendung (Java/Spring Boot, React), die Notaufnahme-Patient:innen in Echtzeit nach dem europäischen Manchester Triage System priorisiert. JWT-Auth mit Rollenmodell, WebSocket-Live-Updates über alle Clients, Flyway-Migrationen, vollständige Testsuite und CI-Pipeline. Im Zuge der Entwicklung einen kritischen Sortier-Bug (alphabetisch statt klinisch) identifiziert und mit Regressionstest behoben. Kostenlos live deployed auf Vercel/Render/Neon.

**Stack:** Java 21 · Spring Boot 3 · Spring Security · WebSocket/STOMP · PostgreSQL · React 19 · Vite · Tailwind · Docker · GitHub Actions

[Live-Demo](https://manchester-triage-monitor.vercel.app) · [GitHub](https://github.com/mdacoding/manchester-triage-monitor)

---

## 3. Ausführliche Case Study

### Problem

Notaufnahmen priorisieren Patient:innen nach dem Manchester Triage System (MTS): fünf Dringlichkeitsstufen (ROT → BLAU) mit klar definierten maximalen Wartezeiten (0–120 Minuten). Ziel des Projekts war es, dieses Verfahren als digitales, für mehrere Arbeitsplätze gleichzeitig nutzbares Dashboard abzubilden – mit den Anforderungen, die eine echte Klinik-Software an Sicherheit, Nachvollziehbarkeit und Verlässlichkeit stellt, aber im Scope eines realistischen Portfolio-Projekts.

### Lösung

Ein Spring-Boot-3-Backend verwaltet den Patientenstatus und broadcastet jede Änderung (neue Aufnahme, Re-Triage, Entlassung) per STOMP/WebSocket an alle verbundenen React-Clients – ohne Polling, in Echtzeit. Die Zugriffskontrolle erfolgt über JWT mit Rollenmodell (Pflegepersonal/Admin), sowohl für REST-Endpunkte als auch für die WebSocket-Verbindung selbst. Datenbankänderungen laufen versioniert über Flyway-Migrationen.

### Die entscheidende Herausforderung: ein Patientensicherheits-Bug

Beim finalen Smoke-Test vor dem Launch fiel auf: Die Warteliste zeigte GRÜN (niedrige Dringlichkeit) vor ROT (Lebensgefahr) an – exakt umgekehrt zur klinischen Realität. Ursache: Die Triagestufe wurde als `EnumType.STRING` in PostgreSQL gespeichert, ein SQL-`ORDER BY` auf dieser Spalte sortierte deshalb alphabetisch statt nach klinischer Dringlichkeit.

In einer echten Notaufnahme hätte das bedeutet, dass Personal mit der geringsten Dringlichkeit zuerst behandelt – ein direktes Patientensicherheitsrisiko. Die Lösung: die Sortierung aus der Datenbankabfrage heraus in die Service-Schicht verlagert, dort mit einem expliziten Java-`Comparator` (Triagestufe → Ankunftszeit) sortiert, und einen dedizierten Regressionstest (`getSortedQueue_ordersByClinicalUrgency_notAlphabetically`) ergänzt, der genau dieses Szenario abdeckt und zukünftige Regressionen verhindert.

Diese Erfahrung hat den Blick dafür geschärft, wie leicht sich Persistenz-Details (hier: Enum-als-String) in fachliche Fehler mit realen Konsequenzen übersetzen können – und warum Domain-Logik mit fachlicher Tragweite nicht implizit der Datenbank überlassen werden sollte.

### Ergebnis

- Vollständige Testsuite (JUnit 5, MockMvc, Spring Security Test) inkl. Regressionstest für die Sortierlogik
- CI-Pipeline (GitHub Actions) für Backend- und Frontend-Build bei jedem Push
- Dockerisiertes Backend, Infrastructure-as-Code-Deployment (Render Blueprint)
- Live-Demo kostenlos deployed auf Vercel (Frontend), Render (Backend) und Neon (PostgreSQL) – ohne laufende Kosten
- Öffentliches GitHub-Repo mit vollständiger, nachvollziehbarer Commit-Historie und ausführlicher README

**Tech-Stack:** Java 21, Spring Boot 3.3 (WebSocket/STOMP, Security, Data JPA, Actuator), PostgreSQL 16, Flyway, JWT, React 19, Vite, Tailwind CSS, Docker, GitHub Actions

**Links:** [Live-Demo](https://manchester-triage-monitor.vercel.app) · [Backend/Swagger UI](https://triage-dashboard-api-dr0z.onrender.com/swagger-ui.html) · [GitHub-Repository](https://github.com/mdacoding/manchester-triage-monitor)
