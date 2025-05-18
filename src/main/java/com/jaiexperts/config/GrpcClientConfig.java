package com.jaiexperts.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.python-core.address}")
    private String pythonCoreAddress;

    @Bean
    public ManagedChannel pythonCoreChannel() {
        return ManagedChannelBuilder
            .forTarget(pythonCoreAddress.replace("static://", ""))
            .usePlaintext()
            .enableRetry()
            .maxRetryAttempts(3)
            .build();
    }
}