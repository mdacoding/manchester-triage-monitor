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
     */
    PatientResponseDto toDto(PatientCase entity);

    /**
     * Mappt eine Liste von Entities auf eine Liste von Response-DTOs.
     */
    List<PatientResponseDto> toDtoList(List<PatientCase> entities);
}
