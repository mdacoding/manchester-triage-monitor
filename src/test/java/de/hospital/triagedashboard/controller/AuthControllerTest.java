package de.hospital.triagedashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hospital.triagedashboard.dto.LoginRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstest für Login und die daran anschließende Bearer-Token-Prüfung.
 *
 * Nutzt den vollen Anwendungskontext (inkl. {@link de.hospital.triagedashboard.security.SecurityConfig}
 * und den vom {@link de.hospital.triagedashboard.config.DemoUserSeeder} angelegten Demo-Nutzern),
 * gegen die H2-Testdatenbank.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/login mit korrekten Demo-Zugangsdaten liefert ein JWT")
    void login_withValidCredentials_returnsToken() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("pflege")
                .password("pflege123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("pflege"))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    @DisplayName("POST /api/auth/login mit falschem Passwort liefert 401 Unauthorized")
    void login_withInvalidPassword_returns401() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("pflege")
                .password("falsches-passwort")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/triage/queue ohne Token liefert 401 Unauthorized")
    void getQueue_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/triage/queue"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/triage/queue mit gültigem Token liefert 200 OK")
    void getQueue_withValidToken_returns200() throws Exception {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("admin")
                .password("admin123!")
                .build();

        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseBody).get("token").asText();

        mockMvc.perform(get("/api/triage/queue")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /swagger-ui.html ist ohne Token erreichbar")
    void swaggerUi_withoutToken_isPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("CORS-Preflight von der Live-Vercel-Origin wird erlaubt")
    void login_preflightFromVercelOrigin_allowsCors() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://manchester-triage-monitor.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "https://manchester-triage-monitor.vercel.app"));
    }
}
