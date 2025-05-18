package com.jaiexperts.config;

import com.jaiexperts.adapter.inbound.CustomWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import java.util.Map;

@Configuration
@EnableWebFlux
public class WebSocketConfig {
    @Bean
    public SimpleUrlHandlerMapping webSocketMapping(CustomWebSocketHandler handler) {
        return new SimpleUrlHandlerMapping(
            Map.of("/ws/chat", handler), 
            1
        );
    }


}