package de.hospital.triagedashboard.service;

import de.hospital.triagedashboard.model.PatientCase;
import de.hospital.triagedashboard.model.TriageLevel;
import de.hospital.triagedashboard.repository.PatientCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Kerngeschäftslogik für die Triage-Warteliste der Notaufnahme.
 *
 * Implementiert das Manchester-Triage-System (MTS):
 *   – Patienten werden nach Dringlichkeitsstufe und Ankunftszeit geordnet.
 *   – Eine Verschlechterung des Zustands (Re-Triage) kann jederzeit
 *     registriert werden; der Patient rückt dann automatisch vor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriageQueueService {

    private final PatientCaseRepository patientCaseRepository;

    /**
     * Nimmt einen neuen Patientenfall auf und berechnet automatisch:
     *   1. Die Ankunftszeit (aktueller Zeitstempel)
     *   2. Die geschätzte maximale Behandlungszeit gemäß MTS-Vorgabe
     *
     * @param patientCase Vorbefüllte Entität mit Name, Triagestufe und Symptomen
     * @return Gespeicherter Patientenfall mit gesetzter ID und Zeitstempeln
     */
    @Transactional
    public PatientCase addPatientToQueue(PatientCase patientCase) {
        LocalDateTime now = LocalDateTime.now();
        patientCase.setAdmissionTime(now);

        // Geschätzte Behandlungszeit = Ankunft + klinisch definierte Maximalwartezeit der Triagestufe
        int maxWaitingMinutes = patientCase.getTriageLevel().getMaxWaitingTimeMinutes();
        patientCase.setEstimatedTreatmentTime(now.plusMinutes(maxWaitingMinutes));

        PatientCase savedCase = patientCaseRepository.save(patientCase);
        log.info("Neuer Patientenfall aufgenommen: id={}, name='{}', triage={}, estimatedTreatment={}",
                savedCase.getId(),
                savedCase.getPatientName(),
                savedCase.getTriageLevel(),
                savedCase.getEstimatedTreatmentTime());
        return savedCase;
    }

    /**
     * Liefert die komplette, klinisch priorisierte Warteliste aller aktiven Fälle.
     *
     * Sortierkriterien (streng hierarchisch):
     *   1. TriageLevel aufsteigend nach Ordinal (RED = 0 zuerst, BLUE = 4 zuletzt)
     *   2. AdmissionTime aufsteigend (FIFO bei gleicher Triagestufe)
     *
     * Die Sortierung nach Triagestufe erfolgt bewusst in Java über
     * {@link TriageLevel#compareTo}, NICHT per Datenbank-ORDER-BY: Da
     * {@code triageLevel} als String persistiert wird (siehe {@link PatientCase}),
     * würde eine SQL-seitige Sortierung alphabetisch statt nach klinischer
     * Dringlichkeit erfolgen (z. B. GREEN vor RED) – ein patientensicherheits-
     * kritischer Fehler.
     *
     * @return Geordnete Liste aktiver Patientenfälle; leer, wenn keine Fälle vorliegen
     */
    @Transactional(readOnly = true)
    public List<PatientCase> getSortedQueue() {
        return patientCaseRepository.findAllActiveOrderByAdmissionTime().stream()
                .sorted(Comparator.comparing(PatientCase::getTriageLevel)
                        .thenComparing(PatientCase::getAdmissionTime))
                .toList();
    }

    /**
     * Aktualisiert die Triagestufe eines bestehenden Falls (Re-Triage).
     *
     * Klinischer Hintergrund: Der Zustand eines Patienten kann sich in der
     * Wartezeit verschlechtern (z. B. von YELLOW zu RED). Die Behandlungszeit
     * wird in diesem Fall neu berechnet, damit keine klinischen Fristen verletzt
     * werden.
     *
     * @param caseId   UUID des zu aktualisierenden Patientenfalls
     * @param newLevel Neue, klinisch festgestellte Triagestufe
     * @return Aktualisierter Patientenfall
     * @throws NoSuchElementException Wenn kein Fall mit der angegebenen ID existiert
     */
    @Transactional
    public PatientCase updateTriageLevel(UUID caseId, TriageLevel newLevel) {
        PatientCase existingCase = patientCaseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Patientenfall nicht gefunden: id=" + caseId));

        TriageLevel previousLevel = existingCase.getTriageLevel();
        existingCase.setTriageLevel(newLevel);

        // Behandlungszeit neu berechnen – Ausgangspunkt bleibt die ursprüngliche Ankunftszeit
        existingCase.setEstimatedTreatmentTime(
                existingCase.getAdmissionTime().plusMinutes(newLevel.getMaxWaitingTimeMinutes()));

        PatientCase updatedCase = patientCaseRepository.save(existingCase);
        log.info("Re-Triage: id={}, name='{}', {} -> {}, neue Behandlungszeit={}",
                updatedCase.getId(),
                updatedCase.getPatientName(),
                previousLevel,
                newLevel,
                updatedCase.getEstimatedTreatmentTime());
        return updatedCase;
    }

    /**
     * Archiviert einen Patientenfall (Entlassung, Verlegung oder Tod).
     * Archivierte Fälle werden aus der aktiven Warteliste entfernt,
     * bleiben aber für Auditzwecke in der Datenbank erhalten.
     *
     * @param caseId UUID des abzuschließenden Patientenfalls
     * @throws NoSuchElementException Wenn kein Fall mit der angegebenen ID existiert
     */
    @Transactional
    public void archivePatientCase(UUID caseId) {
        PatientCase existingCase = patientCaseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Patientenfall nicht gefunden: id=" + caseId));

        existingCase.setArchived(true);
        existingCase.setArchivedAt(LocalDateTime.now());
        patientCaseRepository.save(existingCase);
        log.info("Patientenfall archiviert: id={}, name='{}'",
                existingCase.getId(), existingCase.getPatientName());
    }

    /**
     * Liefert eine paginierte Historie aller archivierten (abgeschlossenen) Fälle,
     * neueste Archivierung zuerst. Rein lesend – ändert nichts an der aktiven
     * Warteliste und wird von {@link #getSortedQueue()} nicht beeinflusst.
     *
     * @param pageable      Seitengröße/-nummer (Sortierung wird serverseitig fest vorgegeben)
     * @param triageLevel   Optionaler Filter auf eine einzelne Triagestufe; {@code null} = alle Stufen
     * @return Seite mit archivierten Patientenfällen
     */
    @Transactional(readOnly = true)
    public Page<PatientCase> getArchivedHistory(Pageable pageable, TriageLevel triageLevel) {
        Page<PatientCase> page = (triageLevel == null)
                ? patientCaseRepository.findByIsArchivedTrueOrderByArchivedAtDesc(pageable)
                : patientCaseRepository.findByIsArchivedTrueAndTriageLevelOrderByArchivedAtDesc(triageLevel, pageable);
        log.debug("GET history: {} archivierte Faelle (Seite {}/{}, Filter={})",
                page.getNumberOfElements(), page.getNumber(), page.getTotalPages(), triageLevel);
        return page;
    }
}
