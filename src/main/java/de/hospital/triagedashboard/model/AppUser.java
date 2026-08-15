package de.hospital.triagedashboard.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Nutzerkonto für die Demo-Authentifizierung des Dashboards.
 *
 * Bewusst schlank gehalten (kein Passwort-Reset, keine Account-Sperrung,
 * keine Mehr-Faktor-Authentifizierung) – ausreichend für ein Portfolio-Demo,
 * NICHT ausreichend für den produktiven Klinikeinsatz mit echten Patientendaten.
 */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /** BCrypt-Hash des Passworts – niemals das Klartext-Passwort speichern. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
