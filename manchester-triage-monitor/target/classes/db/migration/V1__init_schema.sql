-- V1__init_schema.sql
-- Initiale Datenbankstruktur für das Echtzeit-Triage-Dashboard

CREATE TABLE patient_cases (
    id UUID PRIMARY KEY,
    patient_name VARCHAR(255) NOT NULL,
    triage_level VARCHAR(50) NOT NULL,
    symptoms TEXT,
    admission_time TIMESTAMP NOT NULL,
    estimated_treatment_time TIMESTAMP,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE
);

-- Indizes zur Optimierung der Abfragen für die Warteliste
CREATE INDEX idx_patient_cases_active_queue 
ON patient_cases (is_archived, triage_level, admission_time);
