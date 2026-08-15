-- V2__add_users.sql
-- Nutzerkonten fuer die Demo-Authentifizierung (JWT-Login).
-- Passwoerter werden ausschliesslich als BCrypt-Hash gespeichert.

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
