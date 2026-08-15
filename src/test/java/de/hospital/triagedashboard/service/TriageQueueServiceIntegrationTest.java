package de.hospital.triagedashboard.service;

import de.hospital.triagedashboard.model.PatientCase;
import de.hospital.triagedashboard.model.TriageLevel;
import de.hospital.triagedashboard.repository.PatientCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    // Regressionstest: Sortierung darf NICHT alphabetisch nach Enum-Namen
    // erfolgen (BLUE, GREEN, ORANGE, RED, YELLOW), sondern nach klinischer
    // Dringlichkeit (RED, ORANGE, YELLOW, GREEN, BLUE). Da TriageLevel als
    // String persistiert wird, würde eine naive DB-ORDER-BY-Sortierung genau
    // diesen Fehler produzieren (siehe TriageQueueService.getSortedQueue()).
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Warteliste sortiert nach klinischer Dringlichkeit, nicht alphabetisch nach Enum-Namen")
    void getSortedQueue_ordersByClinicalUrgency_notAlphabetically() {
        // Bewusste Einfügereihenfolge, die bei alphabetischer Sortierung
        // (BLUE < GREEN < ORANGE < RED < YELLOW) ein falsches Ergebnis ergäbe.
        triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Gustav Gruen").triageLevel(TriageLevel.GREEN).symptoms("Leichte Beschwerden").build());
        triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Otto Orange").triageLevel(TriageLevel.ORANGE).symptoms("Sehr dringend").build());
        triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Rudi Rot").triageLevel(TriageLevel.RED).symptoms("Lebensgefahr").build());
        triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Berta Blau").triageLevel(TriageLevel.BLUE).symptoms("Nicht dringend").build());
        triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Yvonne Gelb").triageLevel(TriageLevel.YELLOW).symptoms("Dringend").build());

        List<PatientCase> sortedQueue = triageQueueService.getSortedQueue();

        assertThat(sortedQueue)
                .as("Reihenfolge muss RED, ORANGE, YELLOW, GREEN, BLUE sein (klinische Dringlichkeit)")
                .extracting(PatientCase::getTriageLevel)
                .containsExactly(
                        TriageLevel.RED, TriageLevel.ORANGE, TriageLevel.YELLOW,
                        TriageLevel.GREEN, TriageLevel.BLUE);
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

    // ─────────────────────────────────────────────────────────────────────────
    // Patientenhistorie/Archiv-Ansicht: archivierte Faelle erscheinen dort,
    // nicht mehr aber in getSortedQueue() (siehe Test oben) – hier zusaetzlich
    // die Sortierung (neueste Archivierung zuerst) und Pagination.
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("archivePatientCase setzt archivedAt und der Fall erscheint in getArchivedHistory")
    void archivePatientCase_setsArchivedAt_andAppearsInHistory() {
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

        triageQueueService.archivePatientCase(savedArchived.getId());

        Page<PatientCase> history = triageQueueService.getArchivedHistory(PageRequest.of(0, 20), null);

        assertThat(history.getContent()).hasSize(1);
        PatientCase historyEntry = history.getContent().get(0);
        assertThat(historyEntry.getPatientName()).isEqualTo("Klaus Entlassen");
        assertThat(historyEntry.getArchivedAt())
                .as("archivedAt muss beim Archivieren gesetzt werden")
                .isNotNull();

        // Und weiterhin nicht mehr in der aktiven Warteliste (Regressionsschutz)
        assertThat(triageQueueService.getSortedQueue())
                .extracting(PatientCase::getPatientName)
                .containsExactly("Maria Aktiv");
    }

    @Test
    @DisplayName("getArchivedHistory sortiert nach Archivierungszeitpunkt (neueste zuerst) und paginiert korrekt")
    void getArchivedHistory_ordersByArchivedAtDesc_andPaginates() throws InterruptedException {
        PatientCase first = triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Anna Zuerst-Archiviert").triageLevel(TriageLevel.GREEN).symptoms("Fall 1").build());
        PatientCase second = triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Bernd Zweitens-Archiviert").triageLevel(TriageLevel.YELLOW).symptoms("Fall 2").build());
        PatientCase third = triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Carla Zuletzt-Archiviert").triageLevel(TriageLevel.RED).symptoms("Fall 3").build());

        triageQueueService.archivePatientCase(first.getId());
        Thread.sleep(10);
        triageQueueService.archivePatientCase(second.getId());
        Thread.sleep(10);
        triageQueueService.archivePatientCase(third.getId());

        // Seite 1 (Groesse 2): die zwei zuletzt archivierten Faelle
        Page<PatientCase> firstPage = triageQueueService.getArchivedHistory(PageRequest.of(0, 2), null);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getContent())
                .extracting(PatientCase::getPatientName)
                .containsExactly("Carla Zuletzt-Archiviert", "Bernd Zweitens-Archiviert");

        // Seite 2: der aelteste archivierte Fall
        Page<PatientCase> secondPage = triageQueueService.getArchivedHistory(PageRequest.of(1, 2), null);
        assertThat(secondPage.getContent())
                .extracting(PatientCase::getPatientName)
                .containsExactly("Anna Zuerst-Archiviert");
    }

    @Test
    @DisplayName("getArchivedHistory filtert optional nach Triagestufe")
    void getArchivedHistory_filtersByTriageLevel_whenProvided() {
        PatientCase redCase = triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Rudi Rot").triageLevel(TriageLevel.RED).symptoms("Lebensgefahr").build());
        PatientCase greenCase = triageQueueService.addPatientToQueue(PatientCase.builder()
                .patientName("Gustav Gruen").triageLevel(TriageLevel.GREEN).symptoms("Leichte Beschwerden").build());

        triageQueueService.archivePatientCase(redCase.getId());
        triageQueueService.archivePatientCase(greenCase.getId());

        Page<PatientCase> redOnly = triageQueueService.getArchivedHistory(PageRequest.of(0, 20), TriageLevel.RED);

        assertThat(redOnly.getContent())
                .as("Nur RED-Faelle duerfen bei aktivem Filter zurueckgegeben werden")
                .extracting(PatientCase::getPatientName)
                .containsExactly("Rudi Rot");
    }
}
