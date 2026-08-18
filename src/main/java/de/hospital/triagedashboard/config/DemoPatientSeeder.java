package de.hospital.triagedashboard.config;

import de.hospital.triagedashboard.model.PatientCase;
import de.hospital.triagedashboard.model.TriageLevel;
import de.hospital.triagedashboard.repository.PatientCaseRepository;
import de.hospital.triagedashboard.service.TriageQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Legt beim Start eine kleine Demo-Warteliste an, wenn noch keine aktiven
 * Fälle existieren. Damit die Portfolio-Live-Demo nicht leer startet.
 * Bereits vorhandene Daten (echte Demo-Aufnahmen) werden nicht überschrieben.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DemoPatientSeeder implements CommandLineRunner {

    private final PatientCaseRepository patientCaseRepository;
    private final TriageQueueService triageQueueService;

    @Override
    public void run(String... args) {
        if (patientCaseRepository.countByIsArchivedFalse() > 0) {
            return;
        }

        seed("Rudi Rot", TriageLevel.RED,
                "Bewusstlosigkeit, keine Spontanatmung – Reanimationspflichtig");
        seed("Omar Orange", TriageLevel.ORANGE,
                "Starke Brustschmerzen mit Ausstrahlung in den linken Arm");
        seed("Yvonne Gelb", TriageLevel.YELLOW,
                "Fieber 39,2 °C, zunehmende Dyspnoe seit heute Morgen");
        seed("Greta Grün", TriageLevel.GREEN,
                "Verstauchter Sprunggelenk nach Umknicken, belastbar mit Schmerz");
        seed("Berta Blau", TriageLevel.BLUE,
                "Rezeptverlängerung, allgemein unverändertes Befinden");

        log.info("Demo-Warteliste angelegt: {} aktive Faelle",
                patientCaseRepository.countByIsArchivedFalse());
    }

    private void seed(String name, TriageLevel level, String symptoms) {
        PatientCase patientCase = PatientCase.builder()
                .patientName(name)
                .triageLevel(level)
                .symptoms(symptoms)
                .build();
        triageQueueService.addPatientToQueue(patientCase);
    }
}
