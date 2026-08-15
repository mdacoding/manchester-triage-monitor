package de.hospital.triagedashboard.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Validiert das JWT beim STOMP-CONNECT-Handshake.
 *
 * Der Client muss beim Verbindungsaufbau einen nativen STOMP-Header
 * {@code Authorization: Bearer <token>} mitsenden (siehe Frontend
 * {@code useTriageWebSocket}). Ohne gültiges Token wird die Verbindung
 * abgelehnt – damit sind auch die Echtzeit-Updates (Patientennamen,
 * Symptome) nicht ohne Authentifizierung einsehbar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.warn("WebSocket-CONNECT ohne Authorization-Header abgelehnt");
                throw new BadCredentialsException("Kein gültiges Token für WebSocket-Verbindung");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(token, userDetails.getUsername())) {
                log.warn("WebSocket-CONNECT mit ungültigem/abgelaufenem Token abgelehnt: user={}", username);
                throw new BadCredentialsException("Token ungültig oder abgelaufen");
            }

            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()));
            log.debug("WebSocket-CONNECT authentifiziert: user={}", username);
        }

        return message;
    }
}
