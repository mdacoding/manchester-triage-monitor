package de.hospital.triagedashboard.repository;

import de.hospital.triagedashboard.model.PatientCase;
import de.hospital.triagedashboard.model.TriageLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository für {@link PatientCase}-Entitäten.
 *
 * Die komplexe Sortierlogik (Triagestufe → FIFO) wird über eine
 * JPQL-Abfrage implementiert, da die Ordinal-Reihenfolge des Enums
 * direkt der klinischen Priorität entspricht.
 */
@Repository
public interface PatientCaseRepository extends JpaRepository<PatientCase, UUID> {

    /**
     * Liefert alle aktiven (nicht archivierten) Fälle in der korrekten
     * klinischen Behandlungsreihenfolge:
     *   1. Triagestufe aufsteigend nach Ordinal (RED = 0 zuerst)
     *   2. Bei gleicher Stufe: älteste Ankunftszeit zuerst (FIFO)
     */
    @Query("SELECT p FROM PatientCase p WHERE p.isArchived = false ORDER BY p.triageLevel ASC, p.admissionTime ASC")
    List<PatientCase> findAllActiveOrderByTriageLevelAndAdmissionTime();

    /**
     * Zählt aktive Fälle einer bestimmten Triagestufe – nützlich für
     * Kapazitäts- und Auslastungsübersichten auf dem Dashboard.
     */
    long countByTriageLevelAndIsArchivedFalse(TriageLevel triageLevel);
}
