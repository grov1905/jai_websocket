package com.jaiexperts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebFluxSecurity
@Profile("prod")
public class SecurityProdConfig {

    @Bean
    public SecurityWebFilterChain securityWithJwt(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/ws/chat").authenticated()
                .anyExchange().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(org.springframework.core.env.Environment env) {
        String secret = env.getProperty("JWT_SECRET", "defaultsecret");
        String algorithm = env.getProperty("JWT_ALGORITHM", "HmacSHA256");
        byte[] keyBytes = secret.getBytes();

        return NimbusReactiveJwtDecoder
            .withSecretKey(new SecretKeySpec(keyBytes, algorithm))
            .build();
    }
}
