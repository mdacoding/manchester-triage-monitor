package de.hospital.triagedashboard.service;

import de.hospital.triagedashboard.model.PatientCase;
import de.hospital.triagedashboard.model.TriageLevel;
import de.hospital.triagedashboard.repository.PatientCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integrationstests für {@link TriageQueueService}.
 *
 * Verwendet @SpringBootTest, um den echten Anwendungskontext (inkl. H2-Datenbank
 * und Spring Data JPA) zu laden. @Transactional sorgt für automatisches Rollback
 * nach jedem Test – die Tests sind dadurch vollständig isoliert.
 */
@SpringBootTest
@Transactional
class TriageQueueServiceIntegrationTest {

    @Autowired
    private TriageQueueService triageQueueService;

    @Autowired
    private PatientCaseRepository patientCaseRepository;

    @BeforeEach
    void clearDatabase() {
        patientCaseRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Kerntest: RED springt an die Spitze, selbst wenn YELLOW-Patienten
    //           bereits länger warten (primäre Anforderung)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RED-Patient springt an Warteschlangen-Spitze, obwohl YELLOW-Patienten früher ankamen")
    void redPatientLeadsQueue_whenYellowPatientsAreAlreadyWaiting() throws InterruptedException {
        // GIVEN – zwei YELLOW-Patienten kommen zuerst an
        PatientCase firstYellowPatient = PatientCase.builder()
                .patientName("Max Mustermann")
                .triageLevel(TriageLevel.YELLOW)
                .symptoms("Bauchschmerzen seit 2 Stunden")
                .build();

        PatientCase secondYellowPatient = PatientCase.builder()
                .patientName("Erika Musterfrau")
                .triageLevel(TriageLevel.YELLOW)
                .symptoms("Rueckenschmerzen, leichte Dyspnoe")
                .build();

        triageQueueService.addPatientToQueue(firstYellowPatient);
        // Kurze Pause, damit die Ankunftszeiten sich unterscheiden (FIFO-Test)
        Thread.sleep(50);
        triageQueueService.addPatientToQueue(secondYellowPatient);
        Thread.sleep(50);

        // WHEN – ein RED-Patient trifft ein
        PatientCase redPatient = PatientCase.builder()
                .patientName("Hans Notfall")
                .triageLevel(TriageLevel.RED)
                .symptoms("Bewusstlosigkeit, keine Spontanatmung – Reanimationspflichtig")
                .build();

        triageQueueService.addPatientToQueue(redPatient);

        // THEN – Warteschlange: RED → YELLOW (erster) → YELLOW (zweiter)
        List<PatientCase> sortedQueue = triageQueueService.getSortedQueue();

        assertThat(sortedQueue).hasSize(3);

        assertThat(sortedQueue.get(0).getPatientName())
                .as("RED-Patient muss an erster Position stehen")
                .isEqualTo("Hans Notfall");

        assertThat(sortedQueue.get(0).getTriageLevel())
                .as("Erste Position muss Triagestufe RED haben")
                .isEqualTo(TriageLevel.RED);

        assertThat(sortedQueue.get(1).getPatientName())
                .as("Aelterer YELLOW-Patient muss an zweiter Position stehen (FIFO)")
                .isEqualTo("Max Mustermann");

        assertThat(sortedQueue.get(2).getPatientName())
                .as("Juengerer YELLOW-Patient muss an dritter Position stehen (FIFO)")
                .isEqualTo("Erika Musterfrau");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIFO-Reihenfolge bei identischer Triagestufe
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Patienten gleicher Triagestufe werden nach Ankunftszeit (FIFO) sortiert")
    void patientsWithSameTriageLevel_areOrderedByAdmissionTimeFifo() throws InterruptedException {
        PatientCase firstPatient = PatientCase.builder()
                .patientName("Anna Erster")
                .triageLevel(TriageLevel.GREEN)
                .symptoms("Erkältungssymptome")
                .build();

        triageQueueService.addPatientToQueue(firstPatient);
        Thread.sleep(50);

        PatientCase secondPatient = PatientCase.builder()
                .patientName("Bernd Zweiter")
                .triageLevel(TriageLevel.GREEN)
                .symptoms("Kopfschmerzen")
                .build();

        triageQueueService.addPatientToQueue(secondPatient);

        List<PatientCase> sortedQueue = triageQueueService.getSortedQueue();

        assertThat(sortedQueue).hasSize(2);
        assertThat(sortedQueue.get(0).getPatientName())
                .as("Zuerst angekommener Patient muss zuerst behandelt werden")
                .isEqualTo("Anna Erster");
        assertThat(sortedQueue.get(1).getPatientName()).isEqualTo("Bernd Zweiter");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Berechnete Behandlungszeit beim Aufnehmen
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("EstimatedTreatmentTime wird korrekt aus AdmissionTime + MTS-Maximalwartezeit berechnet")
    void estimatedTreatmentTime_isCalculatedCorrectlyOnAdmission() {
        PatientCase orangePatient = PatientCase.builder()
                .patientName("Lena Kritisch")
                .triageLevel(TriageLevel.ORANGE)
                .symptoms("Starke Brustschmerzen, Ausstrahlung in linken Arm")
                .build();

        PatientCase savedCase = triageQueueService.addPatientToQueue(orangePatient);

        assertThat(savedCase.getAdmissionTime()).isNotNull();
        assertThat(savedCase.getEstimatedTreatmentTime()).isNotNull();

        // ORANGE hat max. 10 Minuten Wartezeit gemäß MTS
        assertThat(savedCase.getEstimatedTreatmentTime())
                .as("EstimatedTreatmentTime muss exakt 10 Minuten nach AdmissionTime liegen")
                .isEqualTo(savedCase.getAdmissionTime().plusMinutes(10));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Re-Triage: Triagestufe kann hochgestuft werden
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Re-Triage aktualisiert TriageLevel und berechnet EstimatedTreatmentTime neu")
    void updateTriageLevel_recalculatesEstimatedTreatmentTime() {
        PatientCase initialCase = PatientCase.builder()
                .patientName("Peter Verschlechterung")
                .triageLevel(TriageLevel.YELLOW)
                .symptoms("Leichte Atemnot")
                .build();

        PatientCase savedCase = triageQueueService.addPatientToQueue(initialCase);

        // Zustandsverschlechterung: YELLOW → RED (z. B. Anaphylaxie entwickelt)
        PatientCase upgradedCase = triageQueueService.updateTriageLevel(savedCase.getId(), TriageLevel.RED);

        assertThat(upgradedCase.getTriageLevel())
                .as("Triagestufe muss auf RED aktualisiert sein")
                .isEqualTo(TriageLevel.RED);

        // RED hat maxWaitingTime = 0 → estimatedTreatmentTime == admissionTime
        assertThat(upgradedCase.getEstimatedTreatmentTime())
                .as("Behandlungszeit bei RED muss gleich der Ankunftszeit sein (0 Minuten)")
                .isEqualTo(savedCase.getAdmissionTime().plusMinutes(0));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Archivierte Fälle erscheinen nicht in der Warteschlange
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Archivierte Patientenfaelle werden nicht in der aktiven Warteschlange angezeigt")
    void archivedPatients_areExcludedFromActiveQueue() {
        PatientCase activePatient = PatientCase.builder()
                .patientName("Maria Aktiv")
                .triageLevel(TriageLevel.GREEN)
                .symptoms("Wundversorgung")
                .build();

        PatientCase archivedPatient = PatientCase.builder()
                .patientName("Klaus Entlassen")
                .triageLevel(TriageLevel.BLUE)
                .symptoms("Routineuntersuchung abgeschlossen")
                .build();

        triageQueueService.addPatientToQueue(activePatient);
        PatientCase savedArchived = triageQueueService.addPatientToQueue(archivedPatient);

        // Patient entlassen – Fall archivieren
        triageQueueService.archivePatientCase(savedArchived.getId());

        List<PatientCase> activeQueue = triageQueueService.getSortedQueue();

        assertThat(activeQueue).hasSize(1);
        assertThat(activeQueue.get(0).getPatientName())
                .as("Nur der aktive Patient darf in der Warteliste erscheinen")
                .isEqualTo("Maria Aktiv");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fehlerbehandlung: Unbekannte UUID bei Re-Triage
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTriageLevel wirft NoSuchElementException bei unbekannter Patienten-ID")
    void updateTriageLevel_throwsException_whenCaseIdDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> triageQueueService.updateTriageLevel(nonExistentId, TriageLevel.RED))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(nonExistentId.toString());
    }
}
