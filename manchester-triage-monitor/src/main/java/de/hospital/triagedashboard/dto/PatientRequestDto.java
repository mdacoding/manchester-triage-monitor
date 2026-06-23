package de.hospital.triagedashboard.dto;

import de.hospital.triagedashboard.model.TriageLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object für die Aufnahme eines neuen Patientenfalls.
 *
 * Schützt die JPA-Entity {@link de.hospital.triagedashboard.model.PatientCase}
 * vor direktem Zugriff von außen. Nur klinisch relevante Eingabefelder
 * werden über die API akzeptiert; technische Felder (ID, Zeitstempel,
 * isArchived) werden serverseitig gesetzt.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequestDto {

    /** Vollständiger Name des Patienten (Vor- und Nachname) */
    @NotBlank(message = "Patientenname darf nicht leer sein")
    @Size(max = 255, message = "Patientenname darf maximal 255 Zeichen lang sein")
    private String patientName;

    /**
     * Initiale Triagestufe gemäß Manchester-Triage-System.
     * Bestimmt sofort die Position des Patienten in der Warteliste.
     */
    @NotNull(message = "Triagestufe muss angegeben werden")
    private TriageLevel triageLevel;

    /** Freitextbeschreibung der Leitsymptome – Grundlage für die klinische Ersteinschätzung */
    @Size(max = 2000, message = "Symptombeschreibung darf maximal 2000 Zeichen lang sein")
    private String symptoms;
}
