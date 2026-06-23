package de.hospital.triagedashboard.mapper;

import de.hospital.triagedashboard.dto.PatientRequestDto;
import de.hospital.triagedashboard.dto.PatientResponseDto;
import de.hospital.triagedashboard.model.PatientCase;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-20T07:21:40+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class PatientMapperImpl implements PatientMapper {

    @Override
    public PatientCase toEntity(PatientRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        PatientCase.PatientCaseBuilder patientCase = PatientCase.builder();

        patientCase.patientName( dto.getPatientName() );
        patientCase.triageLevel( dto.getTriageLevel() );
        patientCase.symptoms( dto.getSymptoms() );

        return patientCase.build();
    }

    @Override
    public PatientResponseDto toDto(PatientCase entity) {
        if ( entity == null ) {
            return null;
        }

        PatientResponseDto.PatientResponseDtoBuilder patientResponseDto = PatientResponseDto.builder();

        patientResponseDto.id( entity.getId() );
        patientResponseDto.patientName( entity.getPatientName() );
        patientResponseDto.triageLevel( entity.getTriageLevel() );
        patientResponseDto.symptoms( entity.getSymptoms() );
        patientResponseDto.admissionTime( entity.getAdmissionTime() );
        patientResponseDto.estimatedTreatmentTime( entity.getEstimatedTreatmentTime() );

        return patientResponseDto.build();
    }

    @Override
    public List<PatientResponseDto> toDtoList(List<PatientCase> entities) {
        if ( entities == null ) {
            return null;
        }

        List<PatientResponseDto> list = new ArrayList<PatientResponseDto>( entities.size() );
        for ( PatientCase patientCase : entities ) {
            list.add( toDto( patientCase ) );
        }

        return list;
    }
}
