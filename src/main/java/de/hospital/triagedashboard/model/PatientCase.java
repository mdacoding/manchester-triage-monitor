package de.hospital.triagedashboard.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repräsentiert einen aktiven oder archivierten Patientenfall in der Notaufnahme.
 *
 * Die geschätzte Behandlungszeit ({@code estimatedTreatmentTime}) wird beim
 * Eintragen des Patienten automatisch aus der Ankunftszeit und der gemäß MTS
 * zulässigen Maximalwartezeit der zugewiesenen {@link TriageLevel} berechnet.
 */
@Entity
@Table(name = "patient_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PatientCase {

    /** Eindeutige, unveränderliche UUID des Patientenfalls */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Vollständiger Name des Patienten (Vor- und Nachname) */
    @Column(name = "patient_name", nullable = false)
    private String patientName;

    /**
     * MTS-Triagestufe – bestimmt die Behandlungsreihenfolge.
     * Gespeichert als String, damit Datenbankeinträge lesbar bleiben.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "triage_level", nullable = false)
    private TriageLevel triageLevel;

    /** Freitextbeschreibung der Leitsymptome des Patienten */
    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    /** Zeitpunkt der Ankunft in der Notaufnahme – Grundlage für FIFO bei gleicher Triagestufe */
    @Column(name = "admission_time", nullable = false)
    private LocalDateTime admissionTime;

    /**
     * Spätester klinisch vertretbarer Behandlungsbeginn.
     * Berechnung: admissionTime + maxWaitingTimeMinutes der zugewiesenen TriageLevel.
     */
    @Column(name = "estimated_treatment_time")
    private LocalDateTime estimatedTreatmentTime;

    /**
     * Gibt an, ob der Fall abgeschlossen (Patient entlassen oder verlegt) ist.
     * Archivierte Fälle erscheinen nicht mehr in der aktiven Warteliste.
     */
    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private boolean isArchived = false;
}
