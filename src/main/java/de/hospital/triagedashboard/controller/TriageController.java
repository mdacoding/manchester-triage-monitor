package de.hospital.triagedashboard.controller;

import de.hospital.triagedashboard.dto.PatientRequestDto;
import de.hospital.triagedashboard.dto.PatientResponseDto;
import de.hospital.triagedashboard.mapper.PatientMapper;
import de.hospital.triagedashboard.model.PatientCase;
import de.hospital.triagedashboard.model.TriageLevel;
import de.hospital.triagedashboard.service.TriageQueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für alle Triage-Operationen der Notaufnahme.
 *
 * Jede zustandsverändernde Operation löst nach der Persistierung
 * automatisch einen WebSocket-Broadcast aus.
 */
@Slf4j
@RestController
@RequestMapping("/api/triage")
@RequiredArgsConstructor
public class TriageController {

    private static final String QUEUE_BROADCAST_TOPIC = "/topic/queue";

    private final TriageQueueService triageQueueService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PatientMapper patientMapper;

    @GetMapping("/queue")
    public ResponseEntity<List<PatientResponseDto>> getSortedQueue() {
        List<PatientCase> queue = triageQueueService.getSortedQueue();
        log.debug("GET /queue aufgerufen – {} aktive Faelle zurueckgegeben", queue.size());
        return ResponseEntity.ok(patientMapper.toDtoList(queue));
    }

    @PostMapping("/patient")
    public ResponseEntity<PatientResponseDto> admitPatient(@Valid @RequestBody PatientRequestDto dto) {
        PatientCase newCase = patientMapper.toEntity(dto);
        PatientCase savedCase = triageQueueService.addPatientToQueue(newCase);

        broadcastUpdatedQueue("Neuer Patient aufgenommen");

        return ResponseEntity.status(HttpStatus.CREATED).body(patientMapper.toDto(savedCase));
    }

    @PutMapping("/patient/{id}/level")
    public ResponseEntity<PatientResponseDto> updateTriageLevel(
            @PathVariable UUID id,
            @RequestBody TriageLevel newLevel) {

        PatientCase updatedCase = triageQueueService.updateTriageLevel(id, newLevel);

        broadcastUpdatedQueue("Triagestufe aktualisiert");

        return ResponseEntity.ok(patientMapper.toDto(updatedCase));
    }

    @PatchMapping("/patient/{id}/archive")
    public ResponseEntity<Void> archivePatient(@PathVariable UUID id) {
        triageQueueService.archivePatientCase(id);
        
        broadcastUpdatedQueue("Patient archiviert");
        
        return ResponseEntity.noContent().build();
    }

    private void broadcastUpdatedQueue(String reason) {
        List<PatientCase> updatedQueue = triageQueueService.getSortedQueue();
        messagingTemplate.convertAndSend(QUEUE_BROADCAST_TOPIC, patientMapper.toDtoList(updatedQueue));
        log.info("WS-Broadcast [{}]: {} Faelle an {} gesendet",
                reason, updatedQueue.size(), QUEUE_BROADCAST_TOPIC);
    }
}
