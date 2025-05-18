package com.jaiexperts.adapter.inbound;

import com.jaiexperts.adapter.outbound.GrpcChatClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomWebSocketHandlerTest {

    @Mock
    private GrpcChatClient grpcClient;
    
    @Mock
    private WebSocketSession session;
    
    private CustomWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new CustomWebSocketHandler(grpcClient);
        
        when(session.textMessage(anyString())).thenAnswer(inv -> {
            String payload = inv.getArgument(0);
            return new WebSocketMessage(WebSocketMessage.Type.TEXT, payload);
        });
    }

    @Test
    void shouldProcessMessageAndSendResponse() {
        // Configuración del test
        WebSocketMessage message = mock(WebSocketMessage.class);
        when(message.getPayloadAsText()).thenReturn("test message");
        when(session.receive()).thenReturn(Flux.just(message));
        when(session.send(any())).thenReturn(Mono.empty());
        when(grpcClient.sendMessage(anyString()))
            .thenReturn(Mono.just("response from gRPC"));

        // Ejecución
        Mono<Void> result = handler.handle(session);

        // Verificación
        StepVerifier.create(result)
            .verifyComplete();
        
        verify(grpcClient).sendMessage("test message");
        verify(session).send(any());
    }

    @Test
    void shouldHandleGrpcError() {
        // Configuración del test
        WebSocketMessage message = mock(WebSocketMessage.class);
        when(message.getPayloadAsText()).thenReturn("error message");
        when(session.receive()).thenReturn(Flux.just(message));
        when(session.send(any())).thenReturn(Mono.empty());
        when(session.close()).thenReturn(Mono.empty());
        when(grpcClient.sendMessage(anyString()))
            .thenReturn(Mono.error(new RuntimeException("gRPC error")));

        // Ejecución
        Mono<Void> result = handler.handle(session);

        // Verificación
        StepVerifier.create(result)
            .verifyComplete();
        
        verify(session).send(any());
        verify(session).close();
    }
}