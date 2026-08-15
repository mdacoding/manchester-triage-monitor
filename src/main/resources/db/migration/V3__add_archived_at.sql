-- V3__add_archived_at.sql
-- Ergaenzt einen nullable Zeitstempel fuer die Archivierung eines Patientenfalls.
-- Grundlage fuer die "neueste zuerst"-Sortierung in der Patientenhistorie/Archiv-Ansicht
-- (GET /api/triage/history). Bestehende Faelle bleiben unberuehrt (NULL = nie archiviert
-- bzw. vor Einfuehrung dieses Feldes archiviert).

ALTER TABLE patient_cases ADD COLUMN archived_at TIMESTAMP NULL;

-- Index zur Optimierung der paginierten, nach Archivierungszeitpunkt sortierten Abfrage.
CREATE INDEX idx_patient_cases_archived_history
ON patient_cases (is_archived, archived_at DESC);
