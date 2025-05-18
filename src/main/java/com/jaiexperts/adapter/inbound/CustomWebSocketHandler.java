package com.jaiexperts.adapter.inbound;

import com.jaiexperts.adapter.outbound.GrpcChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Component;

@Component
public class CustomWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomWebSocketHandler.class);
    private final GrpcChatClient grpcClient;

    public CustomWebSocketHandler(GrpcChatClient grpcClient) {
        this.grpcClient = grpcClient;
        log.info("WebSocketHandler initialized with gRPC client targeting: chatservice:50051");

    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
            .flatMap(webSocketMessage -> {
                String payload = webSocketMessage.getPayloadAsText();
                log.info("Mensaje recibido: {}", payload);
                
                return grpcClient.sendMessage(payload)
                    .flatMap(response -> session.send(Mono.just(session.textMessage(response))))
                    .then()
                    .onErrorResume(e -> {
                        log.error("Error en WebSocket: ", e);
                        return session.send(Mono.just(session.textMessage("ERROR: " + e.getMessage())))
                            .then(session.close());
                    });
            })
            .then();
    }
}