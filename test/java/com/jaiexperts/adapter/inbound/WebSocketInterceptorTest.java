package com.jaiexperts.adapter.inbound;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class WebSocketInterceptorTest {

    private final WebSocketHandler mockHandler = mock(WebSocketHandler.class);
    private final WebSocketInterceptor interceptor = new WebSocketInterceptor(mockHandler);

    @Test
    void shouldRejectWhenNoToken() {
        // Configuración
        ServerHttpRequest request = MockServerHttpRequest.get("/ws/chat").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebSocketSession session = mock(WebSocketSession.class);
        
        when(session.getHandshakeInfo()).thenReturn(exchange.getHandshakeInfo());
        
        // Ejecución y verificación
        StepVerifier.create(interceptor.handle(session))
            .expectError(SecurityException.class)
            .verify();
    }

    @Test
    void shouldAcceptValidToken() {
        // Configuración
        when(mockHandler.handle(any())).thenReturn(Mono.empty());
        
        ServerHttpRequest request = MockServerHttpRequest.get("/ws/chat")
            .header("Authorization", "Bearer valid-token")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebSocketSession session = mock(WebSocketSession.class);
        
        when(session.getHandshakeInfo()).thenReturn(exchange.getHandshakeInfo());
        
        // Ejecución y verificación
        StepVerifier.create(interceptor.handle(session))
            .verifyComplete();
        
        verify(mockHandler).handle(session);
    }
}