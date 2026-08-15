package de.hospital.triagedashboard.model;

/**
 * Rollen für die rollenbasierte Zugriffskontrolle (RBAC).
 *
 * STAFF  – Pflege-/Rezeptionspersonal: darf Patienten aufnehmen, re-triagieren, archivieren.
 * ADMIN  – Leitungsfunktion: zusätzlich für zukünftige Verwaltungsfunktionen vorgesehen
 *          (z. B. Nutzerverwaltung, Reporting).
 */
public enum Role {
    STAFF,
    ADMIN
}
