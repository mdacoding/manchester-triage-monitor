package de.hospital.triagedashboard.mapper;

import de.hospital.triagedashboard.dto.PatientRequestDto;
import de.hospital.triagedashboard.dto.PatientResponseDto;
import de.hospital.triagedashboard.model.PatientCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct Mapper für die Konvertierung zwischen DTOs und Entitäten.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PatientMapper {

    /**
     * Mappt das Request-DTO auf eine neue Entity.
     * Ignoriert ID, Zeiten und Archivierungs-Flag, da diese serverseitig gesetzt werden.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "admissionTime", ignore = true)
    @Mapping(target = "estimatedTreatmentTime", ignore = true)
    @Mapping(target = "isArchived", ignore = true)
    PatientCase toEntity(PatientRequestDto dto);

    /**
     * Mappt die JPA-Entity auf das Response-DTO zur API-Auslieferung.
     *
     * Die explizite {@code isArchived}-Zuordnung ist notwendig, weil Lomboks
     * generierte Builder-Methode für das boolesche Feld {@code isArchived}
     * wörtlich "isArchived" heißt, waehrend die per JavaBean-Konvention aus
     * dem Getter {@code isArchived()} abgeleitete Property "archived" lautet.
     * Ohne diese Annotation findet MapStruct keine automatische Übereinstimmung
     * und das Feld bliebe stets auf dem Default-Wert {@code false} stehen –
     * unabhängig vom tatsächlichen Archivierungsstatus.
     */
    @Mapping(target = "isArchived", source = "archived")
    PatientResponseDto toDto(PatientCase entity);

    /**
     * Mappt eine Liste von Entities auf eine Liste von Response-DTOs.
     */
    List<PatientResponseDto> toDtoList(List<PatientCase> entities);
}
