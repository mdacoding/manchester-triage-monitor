package de.hospital.triagedashboard.repository;

import de.hospital.triagedashboard.model.PatientCase;
import de.hospital.triagedashboard.model.TriageLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository für {@link PatientCase}-Entitäten.
 *
 * WICHTIG: Die finale klinische Sortierung (Triagestufe → FIFO) erfolgt
 * bewusst NICHT hier per JPQL "ORDER BY p.triageLevel", da {@code triageLevel}
 * mit {@code @Enumerated(EnumType.STRING)} gespeichert wird – eine
 * datenbankseitige Sortierung nach dieser Spalte wäre alphabetisch
 * (BLUE, GREEN, ORANGE, RED, YELLOW) und nicht nach klinischer Dringlichkeit!
 * Die korrekte Sortierung nach Enum-Ordinal erfolgt daher in
 * {@link de.hospital.triagedashboard.service.TriageQueueService#getSortedQueue()}.
 */
@Repository
public interface PatientCaseRepository extends JpaRepository<PatientCase, UUID> {

    /**
     * Liefert alle aktiven (nicht archivierten) Fälle, vorsortiert nach
     * Ankunftszeit (FIFO) als stabile Basis für die anschließende
     * Priorisierung nach Triagestufe im Service.
     */
    @Query("SELECT p FROM PatientCase p WHERE p.isArchived = false ORDER BY p.admissionTime ASC")
    List<PatientCase> findAllActiveOrderByAdmissionTime();

    /**
     * Zählt aktive Fälle einer bestimmten Triagestufe – nützlich für
     * Kapazitäts- und Auslastungsübersichten auf dem Dashboard.
     */
    long countByTriageLevelAndIsArchivedFalse(TriageLevel triageLevel);

    /**
     * Liefert alle archivierten Fälle für die Historien-Ansicht, paginiert
     * und nach Archivierungszeitpunkt absteigend (neueste zuerst) sortiert.
     */
    Page<PatientCase> findByIsArchivedTrueOrderByArchivedAtDesc(Pageable pageable);

    /**
     * Wie {@link #findByIsArchivedTrueOrderByArchivedAtDesc(Pageable)}, zusätzlich
     * gefiltert auf eine einzelne Triagestufe (Query-Parameter {@code triageLevel}
     * im Historien-Endpunkt).
     */
    Page<PatientCase> findByIsArchivedTrueAndTriageLevelOrderByArchivedAtDesc(TriageLevel triageLevel, Pageable pageable);
}
