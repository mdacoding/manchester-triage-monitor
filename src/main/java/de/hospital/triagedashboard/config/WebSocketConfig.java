package de.hospital.triagedashboard.config;

import org.springframework.context.annotation.Configuration;
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
 * CORS: setAllowedOriginPatterns("*") ist für die Entwicklungsphase gesetzt.
 * In der Produktion durch konkrete Frontend-Ursprünge ersetzen.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
}
