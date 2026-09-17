package com.eventmgmt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures STOMP-over-WebSocket for real-time attendance updates.
 *
 * How it works:
 * 1. Organizer's browser connects to /ws endpoint
 * 2. Organizer subscribes to /topic/attendance/{eventId}
 * 3. When a QR is scanned, AttendanceService pushes updated
 *    dashboard stats to that topic
 * 4. Organizer's screen updates in real-time without polling
 *
 * STOMP protocol provides message routing on top of raw WebSocket.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure the message broker.
     *
     * /topic  — broadcast to all subscribers (attendance dashboard)
     * /queue  — send to specific user (future: personal notifications)
     * /app    — prefix for messages sent FROM client to server
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory broker for /topic and /queue destinations
        registry.enableSimpleBroker("/topic", "/queue");

        // Messages sent from client to server must be prefixed with /app
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Register the WebSocket endpoint that clients connect to.
     * SockJS provides fallback for browsers that don't support WebSocket.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Restrict in production
                .withSockJS();                   // Enable SockJS fallback
    }
}