package com.e.bidding.bidding_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WS endpoint for clients to connect
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:5173").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // prefix for messages from server to clients
        registry.enableSimpleBroker("/topic");
        // prefix for messages from client to server
        registry.setApplicationDestinationPrefixes("/app");
    }
}
