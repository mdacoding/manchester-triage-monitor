package de.hospital.triagedashboard.config;

import de.hospital.triagedashboard.model.AppUser;
import de.hospital.triagedashboard.model.Role;
import de.hospital.triagedashboard.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Legt beim Start zwei Demo-Nutzerkonten an, sofern noch keine Nutzer
 * existieren. Ausschließlich für die Portfolio-/Demo-Umgebung gedacht –
 * in einem produktiven Klinikbetrieb würden Nutzerkonten über eine
 * Verwaltungsoberfläche oder Anbindung an ein Identity-Provider-System
 * (z. B. per KIS-SSO) angelegt.
 *
 * Passwörter sind über die Umgebungsvariablen {@code DEMO_STAFF_PASSWORD}
 * und {@code DEMO_ADMIN_PASSWORD} konfigurierbar; ohne Angabe werden die
 * in {@code application.properties} hinterlegten Default-Demo-Passwörter
 * verwendet (siehe README für die Zugangsdaten).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoUserSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedIfMissing("pflege", Role.STAFF);
        seedIfMissing("admin", Role.ADMIN);
    }

    private void seedIfMissing(String username, Role role) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }

        String rawPassword = resolvePassword(username, role);
        AppUser user = AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        appUserRepository.save(user);
        log.info("Demo-Nutzer angelegt: username={}, role={}", username, role);
    }

    private String resolvePassword(String username, Role role) {
        String envVarName = role == Role.ADMIN ? "DEMO_ADMIN_PASSWORD" : "DEMO_STAFF_PASSWORD";
        String fromEnv = System.getenv(envVarName);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        // Fallback ausschließlich für lokale Entwicklung/Demo – siehe README.
        return role == Role.ADMIN ? "admin123!" : "pflege123!";
    }
}
