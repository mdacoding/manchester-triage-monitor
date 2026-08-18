package de.hospital.triagedashboard.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsOriginsTest {

    @Test
    @DisplayName("merge übernimmt konfigurierte Origins, trimmt Leerzeichen und ergänzt Demo-URLs")
    void merge_trimsAndAddsBuiltinDemoOrigins() {
        List<String> origins = CorsOrigins.merge(" https://custom.example  ,http://localhost:3000 ");

        assertThat(origins)
                .contains("https://custom.example", "http://localhost:3000")
                .containsAll(CorsOrigins.BUILTIN_DEMO_ORIGINS)
                .doesNotContain(" https://custom.example  ");
    }

    @Test
    @DisplayName("merge liefert Demo-Origins auch wenn nichts konfiguriert ist")
    void merge_includesBuiltinOriginsWhenConfiguredIsBlank() {
        assertThat(CorsOrigins.merge("")).containsAll(CorsOrigins.BUILTIN_DEMO_ORIGINS);
        assertThat(CorsOrigins.merge(null)).containsAll(CorsOrigins.BUILTIN_DEMO_ORIGINS);
    }
}
