package com.jaiexperts.adapter.inbound;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class WebSocketInterceptor implements WebSocketHandler {

    private final WebSocketHandler delegate;

    public WebSocketInterceptor(WebSocketHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        ServerWebExchange exchange = (ServerWebExchange) session.getHandshakeInfo()
            .getAttributes()
            .get(ServerWebExchange.class.getName());
        
        if (exchange != null) {
            ServerHttpRequest request = exchange.getRequest();
            String token = request.getHeaders().getFirst("Authorization");
            
            if (token == null || !token.startsWith("Bearer ")) {
                return Mono.error(new SecurityException("Token no válido"));
            }
        }
        
        return delegate.handle(session);
    }
}