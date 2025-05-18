package com.jaiexperts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class WebSocketIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldRejectUnauthorizedConnection() {
        WebSocketClient client = new ReactorNettyWebSocketClient();
        
        Mono<Void> result = client.execute(
            URI.create("ws://localhost:" + getPort() + "/ws/chat"),
            session -> Mono.empty()
        );
        
        StepVerifier.create(result)
            .expectErrorMatches(t -> t instanceof SecurityException && 
                t.getMessage().contains("Token no válido"))
            .verify();
    }

    private int getPort() {
        return webTestClient.get().uri("/").exchange()
            .returnResult(Integer.class).getResponseBody().blockFirst();
    }
}