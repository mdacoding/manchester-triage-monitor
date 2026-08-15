package de.hospital.triagedashboard.controller;

import de.hospital.triagedashboard.dto.LoginRequestDto;
import de.hospital.triagedashboard.dto.LoginResponseDto;
import de.hospital.triagedashboard.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login-Endpunkt für die Demo-Authentifizierung.
 *
 * Gibt bei gültigen Zugangsdaten ein JWT zurück, das der Client anschließend
 * als {@code Authorization: Bearer <token>}-Header an alle REST-Aufrufe und
 * beim WebSocket-CONNECT anhängt.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.replace("ROLE_", ""))
                    .orElse("STAFF");

            String token = jwtService.generateToken(userDetails.getUsername(), role);
            log.info("Login erfolgreich: user={}, role={}", userDetails.getUsername(), role);

            return ResponseEntity.ok(LoginResponseDto.builder()
                    .token(token)
                    .username(userDetails.getUsername())
                    .role(role)
                    .expiresInMillis(jwtService.getExpirationMillis())
                    .build());
        } catch (BadCredentialsException ex) {
            log.warn("Fehlgeschlagener Login-Versuch: user={}", request.getUsername());
            throw ex;
        }
    }
}
