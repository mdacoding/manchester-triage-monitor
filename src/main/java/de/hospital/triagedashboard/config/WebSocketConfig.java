package de.hospital.triagedashboard.config;

import de.hospital.triagedashboard.security.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Konfiguration des STOMP-over-WebSocket-Brokers.
 *
 * Kommunikationsflüsse:
 *   Client → Server : Nachrichten werden an Pfade mit Präfix "/app" gesendet
 *                     und von @MessageMapping-Methoden verarbeitet.
 *   Server → Client : Der Simple Broker verteilt Nachrichten an alle
 *                     Abonnenten eines "/topic/..."-Pfads (Pub/Sub).
 *
 * Authentifizierung: {@link StompAuthChannelInterceptor} prüft bei jedem
 * CONNECT-Frame das mitgesendete JWT (siehe dort).
 *
 * CORS: setAllowedOriginPatterns wird über {@code app.cors.allowed-origins}
 * konfiguriert (Default: lokale Vite-Dev-Origin).
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    /**
     * Aktiviert den In-Memory Simple Broker für das /topic-Präfix.
     * Clients können "/topic/queue" abonnieren, um Push-Updates zu empfangen.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registriert den STOMP-Endpunkt für die WebSocket-Verbindung.
     * withSockJS() erlaubt Fallback auf Long-Polling für Clients ohne
     * native WebSocket-Unterstützung.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-triage")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Hängt die JWT-Prüfung in die eingehende Nachrichten-Pipeline ein,
     * bevor CONNECT-Frames verarbeitet werden.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
