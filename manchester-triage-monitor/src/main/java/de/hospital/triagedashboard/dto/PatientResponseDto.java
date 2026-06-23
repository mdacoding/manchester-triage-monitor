package de.hospital.triagedashboard.dto;

import de.hospital.triagedashboard.model.TriageLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object für ausgehende Patienten-Daten.
 * Vermeidet die direkte Serialisierung der JPA-Entity und schützt
 * ggf. interne Felder (z.B. später Audit- oder Security-relevante Daten)
 * vor unbeabsichtigter API-Offenlegung.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDto {
    private UUID id;
    private String patientName;
    private TriageLevel triageLevel;
    private String symptoms;
    private LocalDateTime admissionTime;
    private LocalDateTime estimatedTreatmentTime;
    private boolean isArchived;
}
