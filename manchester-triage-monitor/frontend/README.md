# 🎨 Triage Dashboard Frontend

[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3.4-06B6D4?logo=tailwindcss)](https://tailwindcss.com/)

**Echtzeit-Triage-Dashboard Frontend** für die Notaufnahme. Eine moderne React-Anwendung mit Echtzeit-WebSocket-Kommunikation und klinisch-beruhigendem Design.

---

## ✨ Features

### UI/UX
- 🎨 **Klinisch-beruhigendes Design**: Sanfte Farben und klare Typografie für den Einsatz in Notaufnahmen
- ⚡ **Echtzeit-Updates**: Live-Synchronisation der Warteliste via WebSocket
- 📱 **Responsive Design**: Optimiert für Desktop, Tablet und Mobile
- 🔄 **Automatischer Reconnect**: WebSocket-Verbindung mit Exponential Backoff
- 🎯 **Intuitive Bedienung**: Minimaler Lernaufwand für medizinisches Personal

### Technische Features
- ⚛️ **React 19**: Neueste React-Version mit Hooks
- 🚀 **Vite 8**: Blitzschneller Build und HMR
- 🎨 **Tailwind CSS 3.4**: Utility-first CSS Framework
- 🔌 **STOMP-over-WebSocket**: Echtzeitkommunikation mit @stomp/stompjs
- 🛡️ **Error Boundary**: Robuste Fehlerbehandlung
- 🧪 **ESLint**: Code-Qualität und Consistency

---

## 🏗️ Architektur

```
frontend/
├── src/
│   ├── components/           # Wiederverwendbare UI-Komponenten
│   │   ├── PatientCard.jsx           # Patienten-Karte mit Triage-Farben
│   │   ├── PatientFormModal.jsx      # Modal zum Erfassen neuer Patienten
│   │   ├── TriageSummaryBar.jsx      # Übersicht der Triage-Stufen
│   │   ├── ConnectionIndicator.jsx   # WebSocket-Verbindungsstatus
│   │   └── ErrorBoundary.jsx         # React Error Boundary
│   │
│   ├── hooks/                # Custom React Hooks
│   │   └── useTriageWebSocket.js     # WebSocket-Lebenszyklus-Management
│   │
│   ├── pages/                # Seiten-Komponenten
│   │   └── TriageDashboard.jsx       # Hauptdashboard-Ansicht
│   │
│   ├── config/               # Konfiguration
│   │   └── triageConfig.js           # MTS-Farben und Labels
│   │
│   ├── utils/                # Hilfsfunktionen
│   │   └── timeUtils.js              # Zeitformatierung und -berechnung
│   │
│   ├── App.jsx               # Root-Komponente
│   ├── main.jsx              # Entry Point
│   └── index.css             # Globale Styles + Tailwind
│
├── public/                   # Statische Assets
├── package.json              # Dependencies und Scripts
├── vite.config.js            # Vite-Konfiguration mit Proxy
├── tailwind.config.js        # Tailwind-Konfiguration
└── eslint.config.js          # ESLint-Regeln
```

---

## 🚀 Quick Start

### Voraussetzungen

- Node.js 18+ und npm
- Laufendes Backend auf `http://localhost:8080`

### Installation

```bash
# Dependencies installieren
npm install

# Development-Server starten
npm run dev
```

Die Anwendung ist dann verfügbar unter: **http://localhost:5173**

---

## 📦 Verfügbare Scripts

```bash
# Development-Server mit HMR
npm run dev

# Production-Build erstellen
npm run build

# Build-Vorschau
npm run preview

# ESLint ausführen
npm run lint
```

---

## 🔌 WebSocket-Kommunikation

### Custom Hook: `useTriageWebSocket`

Der Hook kapselt die gesamte WebSocket-Logik:

```javascript
const { queue, connectionStatus } = useTriageWebSocket()
```

**Connection States:**
- `CONNECTING` – Verbindung wird aufgebaut
- `CONNECTED` – Live-Verbindung aktiv
- `ERROR` – Verbindungsfehler

**Automatisches Reconnect:**
- 5 Sekunden Delay nach Verbindungsabbruch
- Exponential Backoff durch @stomp/stompjs Client

---

## 🎨 Design-System

### Triage-Farben (MTS)

Die Farbcodierung folgt dem Manchester-Triage-System:

```javascript
import { TRIAGE_CONFIG } from './config/triageConfig'

// Verfügbare Stufen:
// RED, ORANGE, YELLOW, GREEN, BLUE

// Jede Stufe hat:
// - label: Anzeigename
// - borderColor: Linker Rand der Patientenkarte
// - dotColor: Status-Indikator
// - badgeBg / badgeText: Badge-Hintergrund und Text
```

### Tailwind-Klassen

Das Projekt verwendet ein **konsistentes Design-System**:

- **Farben**: Stone-Palette (neutral, klinisch-beruhigend)
- **Abstände**: 4px-Raster (Tailwind Standard)
- **Typografie**: System-UI-Fonts mit klarer Hierarchie
- **Schatten**: Sanfte, mehrschichtige Schatten für Tiefe
- **Radius**: 8-12px für moderne, weiche Ecken

---

## 🧩 Komponenten

### PatientCard

Zeigt einen einzelnen Patientenfall mit:
- Wartepositions-Nummer
- Patientendaten (Name, Symptome)
- Triage-Badge mit Farbcodierung
- Zeitmetadaten (Ankunft, Wartezeit, Behandlung bis)
- Archivierungs-Button

### PatientFormModal

Modal zum Erfassen neuer Patienten:
- Pflichtfeld: Name
- Pflichtfeld: Triagestufe
- Optional: Symptombeschreibung
- Client-seitige Validierung
- Fehlerbehandlung

### TriageSummaryBar

Übersicht der aktuellen Warteliste:
- Anzahl Patienten pro Triage-Stufe
- Farbcodierte Indikatoren
- Kompakte Darstellung

### ConnectionIndicator

Zeigt den WebSocket-Status:
- 🟢 Live (verbunden)
- 🟡 Verbinde … (aufbauend)
- 🔴 Verbindungsfehler

---

## 🔧 Konfiguration

### Vite-Proxy

Der Development-Server proxyt API- und WebSocket-Anfragen an das Backend:

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': { target: 'http://localhost:8080', changeOrigin: true },
    '/ws-triage': { target: 'http://localhost:8080', ws: true, changeOrigin: true }
  }
}
```

**Vorteil:** Keine CORS-Probleme während der Entwicklung.

### Umgebungsvariablen

Für Production-Deployments können Umgebungsvariablen verwendet werden:

```bash
# .env (nicht im Repo)
VITE_API_URL=https://api.production.com
VITE_WS_URL=wss://ws.production.com
```

---

## 🧪 Testing

### Manuelles Testen

1. **Backend starten** (siehe Backend-README)
2. **Frontend starten**: `npm run dev`
3. **Browser öffnen**: http://localhost:5173

### Test-Szenarien

- ✅ Neuen Patienten aufnehmen
- ✅ Echtzeit-Update in mehreren Browser-Tabs beobachten
- ✅ Patienten archivieren
- ✅ Verbindungsabbruch simulieren (Backend stoppen)
- ✅ Automatischen Reconnect beobachten

---

## 🎯 Geplante Features

- [ ] Re-Triage-UI (Triagestufe direkt in der Karte ändern)
- [ ] Suchfunktion nach Patientennamen
- [ ] Filter nach Triage-Stufe
- [ ] Dark Mode
- [ ] Mehrsprachigkeit (i18n)
- [ ] Barrierefreiheit (WCAG 2.1)
- [ ] Unit-Tests mit Vitest
- [ ] E2E-Tests mit Playwright

---

## 🛠️ Entwicklung

### Code-Style

- **ESLint**: Automatische Code-Überprüfung
- **Prettier**: Empfohlen für consistent Formatting
- **Komponenten**: Funktionale Komponenten mit Hooks
- **Naming**: camelCase für Variablen, PascalCase für Komponenten

### Best Practices

- ✅ Kleine, wiederverwendbare Komponenten
- ✅ Custom Hooks für komplexe Logik
- ✅ Props mit JSDoc dokumentiert
- ✅ Fehlerbehandlung mit Error Boundary
- ✅ Keine inline Styles (Tailwind-Klassen)

---

## 📦 Build & Deployment

### Production Build

```bash
npm run build
```

Erstellt optimierte Assets im `dist/` Ordner.

### Deployment-Optionen

1. **Vercel / Netlify**: Einfaches Hosting für SPAs
2. **Nginx**: Static File Serving mit Reverse Proxy
3. **Docker**: Multi-Stage Build (siehe Root-README)

---

## 🤝 Beitrag leisten

### Development-Workflow

1. **Branch erstellen**: `git checkout -b feature/AmazingFeature`
2. **Änderungen vornehmen**: Halte Komponenten klein und fokussiert
3. **Linting**: `npm run lint` ausführen
4. **Testen**: Manuell in mehreren Browsern testen
5. **Commit**: `git commit -m 'Add some AmazingFeature'`
6. **Push**: `git push origin feature/AmazingFeature`
7. **Pull Request**: Öffne einen PR mit Beschreibung

---

## 📚 Ressourcen

### Dokumentation

- [React Dokumentation](https://react.dev/)
- [Vite Guide](https://vitejs.dev/guide/)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [@stomp/stompjs](https://stomp-js.github.io/guide/stompjs/using-stompjs-on-react.html)

### Design

- [Tailwind UI](https://tailwindui.com/)
- [shadcn/ui](https://ui.shadcn.com/)
- [Figma Community](https://www.figma.com/community)

---

## 📝 Lizenz

Dieses Projekt wurde im Rahmen einer Studienarbeit / als Demo-Projekt entwickelt.

---

## 🙏 Danksagungen

- **React Team** für die exzellente UI-Bibliothek
- **Vite Team** für den blitzschnellen Build-Prozess
- **Tailwind CSS** für das intuitive Utility-Framework
- **@stomp/stompjs** für die WebSocket-Integration

---

## ⚠️ Hinweis

Dies ist eine **Demo-Anwendung**. Für den Produktivbetrieb müssen zusätzlich implementiert werden:
- Umfassende Unit- und E2E-Tests
- TypeScript für Typsicherheit
- Barrierefreiheit (WCAG 2.1)
- Performance-Optimierungen (Code Splitting, Lazy Loading)
- Security-Headers und CSP

---

**🏥 Entwickelt für den Einsatz in Krankenhaus-Notaufnahmen**