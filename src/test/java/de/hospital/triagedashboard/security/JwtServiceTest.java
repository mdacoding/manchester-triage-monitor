package de.hospital.triagedashboard.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-Tests für {@link JwtService} — reine POJO-Logik, kein Spring-Kontext nötig.
 */
class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-min-32-characters-long";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 1000L * 60 * 60); // 1 Stunde Gültigkeit
    }

    @Test
    @DisplayName("Ein generiertes Token enthält den korrekten Benutzernamen als Subject")
    void generateToken_embedsUsernameAsSubject() {
        String token = jwtService.generateToken("pflege", "STAFF");

        assertThat(jwtService.extractUsername(token)).isEqualTo("pflege");
    }

    @Test
    @DisplayName("Ein generiertes Token enthält die Rolle als Claim")
    void generateToken_embedsRoleClaim() {
        String token = jwtService.generateToken("admin", "ADMIN");

        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Ein frisch generiertes Token ist für den korrekten Benutzer gültig")
    void isTokenValid_returnsTrue_forMatchingUsernameAndFreshToken() {
        String token = jwtService.generateToken("pflege", "STAFF");

        assertThat(jwtService.isTokenValid(token, "pflege")).isTrue();
    }

    @Test
    @DisplayName("Ein Token ist ungültig, wenn der Benutzername nicht übereinstimmt")
    void isTokenValid_returnsFalse_whenUsernameDoesNotMatch() {
        String token = jwtService.generateToken("pflege", "STAFF");

        assertThat(jwtService.isTokenValid(token, "admin")).isFalse();
    }

    @Test
    @DisplayName("Ein abgelaufenes Token gilt als ungültig")
    void isTokenValid_returnsFalse_whenTokenIsExpired() throws InterruptedException {
        JwtService shortLivedJwtService = new JwtService(TEST_SECRET, 1L); // 1ms Gültigkeit
        String token = shortLivedJwtService.generateToken("pflege", "STAFF");

        Thread.sleep(20);

        assertThat(shortLivedJwtService.isTokenValid(token, "pflege")).isFalse();
    }

    @Test
    @DisplayName("Ein mit anderem Secret signiertes Token wird als ungültig erkannt")
    void isTokenValid_returnsFalse_whenSignedWithDifferentSecret() {
        JwtService otherJwtService = new JwtService(
                "a-completely-different-secret-key-with-32-plus-chars", 1000L * 60);
        String token = otherJwtService.generateToken("pflege", "STAFF");

        assertThat(jwtService.isTokenValid(token, "pflege")).isFalse();
    }
}
