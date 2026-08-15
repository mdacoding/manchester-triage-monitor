package de.hospital.triagedashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hospital.triagedashboard.dto.PatientRequestDto;
import de.hospital.triagedashboard.dto.PatientResponseDto;
import de.hospital.triagedashboard.mapper.PatientMapper;
import de.hospital.triagedashboard.model.PatientCase;
import de.hospital.triagedashboard.model.TriageLevel;
import de.hospital.triagedashboard.service.TriageQueueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TriageController.class)
class TriageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TriageQueueService triageQueueService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private PatientMapper patientMapper;

    @Test
    @DisplayName("GET /queue gibt die sortierte Warteliste als JSON zurück")
    void getQueue_returnsJsonListOfActivePatients() throws Exception {
        PatientCase mockCase = buildMockPatientCase(TriageLevel.RED);
        PatientResponseDto dto = buildMockPatientResponseDto(TriageLevel.RED);
        
        when(triageQueueService.getSortedQueue()).thenReturn(List.of(mockCase));
        when(patientMapper.toDtoList(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/triage/queue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].patientName").value("Test Patient"))
                .andExpect(jsonPath("$[0].triageLevel").value("RED"));
    }

    @Test
    @DisplayName("POST /patient nimmt neuen Patienten auf und gibt HTTP 201 zurück")
    void admitPatient_returns201Created_andBroadcastsQueue() throws Exception {
        PatientRequestDto requestDto = PatientRequestDto.builder()
                .patientName("Hans Notfall")
                .triageLevel(TriageLevel.RED)
                .symptoms("Bewusstlosigkeit")
                .build();

        PatientCase savedCase = buildMockPatientCase(TriageLevel.RED);
        PatientResponseDto responseDto = buildMockPatientResponseDto(TriageLevel.RED);
        responseDto = PatientResponseDto.builder()
                .id(savedCase.getId())
                .patientName(requestDto.getPatientName())
                .triageLevel(requestDto.getTriageLevel())
                .build();

        when(patientMapper.toEntity(any())).thenReturn(savedCase);
        when(triageQueueService.addPatientToQueue(any(PatientCase.class))).thenReturn(savedCase);
        when(patientMapper.toDto(any(PatientCase.class))).thenReturn(responseDto);
        when(triageQueueService.getSortedQueue()).thenReturn(List.of(savedCase));
        when(patientMapper.toDtoList(any())).thenReturn(List.of(responseDto));

        mockMvc.perform(post("/api/triage/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientName").value("Hans Notfall"));

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/queue"), any(List.class));
    }

    @Test
    @DisplayName("POST /patient mit fehlendem Namen gibt HTTP 400 Bad Request zurück")
    void admitPatient_returns400_whenPatientNameIsBlank() throws Exception {
        PatientRequestDto invalidDto = PatientRequestDto.builder()
                .patientName("")          
                .triageLevel(TriageLevel.GREEN)
                .build();

        mockMvc.perform(post("/api/triage/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(triageQueueService);
    }

    @Test
    @DisplayName("PUT /patient/{id}/level aktualisiert Triagestufe und sendet WS-Broadcast")
    void updateTriageLevel_returns200_andBroadcastsQueue() throws Exception {
        UUID patientId = UUID.randomUUID();
        PatientCase upgradedCase = buildMockPatientCase(TriageLevel.RED);
        PatientResponseDto dto = buildMockPatientResponseDto(TriageLevel.RED);

        when(triageQueueService.updateTriageLevel(eq(patientId), eq(TriageLevel.RED)))
                .thenReturn(upgradedCase);
        when(patientMapper.toDto(any())).thenReturn(dto);
        when(triageQueueService.getSortedQueue()).thenReturn(List.of(upgradedCase));
        when(patientMapper.toDtoList(any())).thenReturn(List.of(dto));

        mockMvc.perform(put("/api/triage/patient/{id}/level", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TriageLevel.RED)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.triageLevel").value("RED"));

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/queue"), any(List.class));
    }

    @Test
    @DisplayName("PATCH /patient/{id}/archive archiviert Patient und sendet WS-Broadcast")
    void archivePatient_returns204_andBroadcastsQueue() throws Exception {
        UUID patientId = UUID.randomUUID();
        
        when(triageQueueService.getSortedQueue()).thenReturn(List.of());
        when(patientMapper.toDtoList(any())).thenReturn(List.of());

        mockMvc.perform(patch("/api/triage/patient/{id}/archive", patientId))
                .andExpect(status().isNoContent());

        verify(triageQueueService).archivePatientCase(patientId);
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/queue"), any(List.class));
    }

    private PatientCase buildMockPatientCase(TriageLevel level) {
        return PatientCase.builder()
                .id(UUID.randomUUID())
                .patientName("Test Patient")
                .triageLevel(level)
                .symptoms("Testbeschreibung")
                .admissionTime(LocalDateTime.now())
                .estimatedTreatmentTime(LocalDateTime.now().plusMinutes(level.getMaxWaitingTimeMinutes()))
                .build();
    }

    private PatientResponseDto buildMockPatientResponseDto(TriageLevel level) {
        return PatientResponseDto.builder()
                .id(UUID.randomUUID())
                .patientName("Test Patient")
                .triageLevel(level)
                .symptoms("Testbeschreibung")
                .admissionTime(LocalDateTime.now())
                .estimatedTreatmentTime(LocalDateTime.now().plusMinutes(level.getMaxWaitingTimeMinutes()))
                .isArchived(false)
                .build();
    }
}
