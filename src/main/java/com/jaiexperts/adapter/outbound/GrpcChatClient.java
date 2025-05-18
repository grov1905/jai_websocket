package com.jaiexperts.adapter.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaiexperts.chat.ChatRequest;
import com.jaiexperts.chat.ChatResponse;
import com.jaiexperts.chat.ChatServiceGrpc;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;

@Service
public class GrpcChatClient {

    private static final Logger log = LoggerFactory.getLogger(GrpcChatClient.class);
    private final ChatServiceGrpc.ChatServiceStub asyncStub;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GrpcChatClient(ManagedChannel channel) {
        this.asyncStub = ChatServiceGrpc.newStub(channel);
        log.info("Initialized gRPC client for ChatService");
    }

    public Mono<String> sendMessage(String messageJson) {
        return Mono.create(sink -> {
            try {
                JsonNode json = objectMapper.readTree(messageJson);
                
                ChatRequest request = ChatRequest.newBuilder()
                    .setChannel(json.path("channel").asText("websocket")) // Valor por defecto
                    .setExternalId(json.path("external_id").asText(""))
                    .setBusinessId(json.path("business_id").asText(""))
                    .setContent(json.path("content").asText(""))
                    .putAllMetadata(parseMetadata(json.path("metadata")))
                    .build();

                log.debug("Sending gRPC request: {}", request);

                asyncStub.processMessage(request, new StreamObserver<ChatResponse>() {
                    @Override
                    public void onNext(ChatResponse response) {
                        try {
                            // Crear objeto JSON con todos los campos de la respuesta
                            Map<String, Object> responseMap = new HashMap<>();
                            responseMap.put("content", response.getContent());
                            responseMap.put("conversation_id", response.getConversationId());
                            responseMap.put("end_user_id", response.getEndUserId());
                            responseMap.put("external_id", response.getExternalId());

                            // Convertir a JSON string
                            String jsonResponse = objectMapper.writeValueAsString(responseMap);
                            log.info("Received gRPC response: {}", jsonResponse);
                            sink.success(jsonResponse);
                        } catch (Exception e) {
                            log.error("Error serializing response to JSON: ", e);
                            sink.error(e);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("gRPC error: ", t);
                        sink.error(new RuntimeException("gRPC communication error: " + t.getMessage()));
                    }

                    @Override
                    public void onCompleted() {
                        log.debug("gRPC call completed");
                    }
                });
            } catch (Exception e) {
                log.error("Error processing message JSON: ", e);
                sink.error(new RuntimeException("Invalid message format: " + e.getMessage()));
            }
        });
    }

    private Map<String, String> parseMetadata(JsonNode metadataNode) {
        Map<String, String> metadataMap = new HashMap<>();
        
        if (metadataNode.isObject()) {
            metadataNode.fields().forEachRemaining(entry -> {
                String value = entry.getValue().isTextual() ? 
                    entry.getValue().asText() : 
                    entry.getValue().toString();
                metadataMap.put(entry.getKey(), value);
            });
        }
        
        return metadataMap;
    }
}