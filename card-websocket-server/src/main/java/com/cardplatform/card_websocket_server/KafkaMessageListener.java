package com.cardplatform.card_websocket_server;

import com.cardplatform.card_websocket_server.DTO.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageListener {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public KafkaMessageListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @KafkaListener(topics = "game-chat-topic", groupId = "websocket-group", containerFactory = "kafkaListenerContainerFactory")
    public void listenAndBroadcast(ChatMessage chatMessage) {
        System.out.println("Kafka로부터 메시지 수신: " + chatMessage.getMessage());
        // chatMessage 객체를 그대로 broadcastToRoom으로 전달합니다.
        sessionManager.broadcastToRoom(chatMessage);
    }
}