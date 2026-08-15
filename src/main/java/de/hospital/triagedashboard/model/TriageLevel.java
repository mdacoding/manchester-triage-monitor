package de.hospital.triagedashboard.model;

/**
 * Manchester-Triage-System (MTS) Dringlichkeitsstufen.
 *
 * Die Ordinal-Reihenfolge (0 = höchste Priorität) wird direkt für die
 * Sortierung der Warteliste verwendet – RED erhält Ordinal 0 und wird
 * deshalb stets zuerst behandelt.
 *
 * Klinische Bedeutung der Stufen:
 *   RED    – Lebensgefährlicher Zustand, sofortige Intervention erforderlich
 *   ORANGE – Potenziell lebensbedrohlich, sehr rasche Behandlung nötig
 *   YELLOW – Ernstzunehmend, aber stabile Vitalzeichen; zügige Versorgung
 *   GREEN  – Wenig dringlich, ambulanter Charakter; Versorgung kann warten
 *   BLUE   – Keine Dringlichkeit; elektive Weiterleitung möglich
 */
public enum TriageLevel {

    /** Sofort / Lebensgefahr – maximale Wartezeit: 0 Minuten */
    RED(0),

    /** Sehr dringend – maximale Wartezeit: 10 Minuten */
    ORANGE(10),

    /** Dringend – maximale Wartezeit: 30 Minuten */
    YELLOW(30),

    /** Normal – maximale Wartezeit: 90 Minuten */
    GREEN(90),

    /** Nicht dringend – maximale Wartezeit: 120 Minuten */
    BLUE(120);

    /** Klinisch definierte maximale Wartezeit in Minuten gemäß MTS-Leitlinien */
    private final int maxWaitingTimeMinutes;

    TriageLevel(int maxWaitingTimeMinutes) {
        this.maxWaitingTimeMinutes = maxWaitingTimeMinutes;
    }

    public int getMaxWaitingTimeMinutes() {
        return maxWaitingTimeMinutes;
    }
}
