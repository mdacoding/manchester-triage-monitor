package de.hospital.triagedashboard.mapper;

import de.hospital.triagedashboard.dto.PatientResponseDto;
import de.hospital.triagedashboard.model.PatientCase;
import de.hospital.triagedashboard.model.TriageLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regressionstest für {@link PatientMapper}.
 *
 * Hintergrund: Lombok generiert für ein boolesches Feld {@code isArchived}
 * eine Builder-Methode mit dem wörtlichen Feldnamen ({@code isArchived(...)}),
 * waehrend die per JavaBean-Konvention aus dem Getter {@code isArchived()}
 * abgeleitete Property "archived" lautet. Ohne eine explizite
 * {@code @Mapping}-Annotation findet MapStruct dadurch keine automatische
 * Übereinstimmung und das Feld bleibt im generierten DTO stets auf dem
 * Default-Wert {@code false} stehen – unabhängig vom tatsächlichen
 * Archivierungsstatus der Entity. Dieser Test stellt sicher, dass genau
 * dieser Fall nicht unbemerkt wieder auftritt.
 */
class PatientMapperTest {

    private final PatientMapper mapper = new PatientMapperImpl();

    @Test
    @DisplayName("toDto überträgt isArchived=true und archivedAt korrekt (Regressionstest für Lombok-Builder/MapStruct-Fehlmapping)")
    void toDto_mapsArchivedFlagAndTimestamp_forArchivedCase() {
        LocalDateTime archivedAt = LocalDateTime.of(2026, 8, 15, 18, 0);
        PatientCase archivedCase = PatientCase.builder()
                .patientName("Regressionstest-Patient")
                .triageLevel(TriageLevel.GREEN)
                .admissionTime(LocalDateTime.of(2026, 8, 15, 12, 0))
                .isArchived(true)
                .archivedAt(archivedAt)
                .build();

        PatientResponseDto dto = mapper.toDto(archivedCase);

        assertThat(dto.isArchived())
                .as("archivierte Faelle muessen im Response-DTO auch als archiviert markiert sein")
                .isTrue();
        assertThat(dto.getArchivedAt()).isEqualTo(archivedAt);
    }

    @Test
    @DisplayName("toDto überträgt isArchived=false korrekt für aktive Faelle")
    void toDto_mapsArchivedFlag_forActiveCase() {
        PatientCase activeCase = PatientCase.builder()
                .patientName("Aktiver Patient")
                .triageLevel(TriageLevel.RED)
                .admissionTime(LocalDateTime.of(2026, 8, 15, 12, 0))
                .isArchived(false)
                .build();

        PatientResponseDto dto = mapper.toDto(activeCase);

        assertThat(dto.isArchived()).isFalse();
        assertThat(dto.getArchivedAt()).isNull();
    }
}
