package com.cardplatform.card_websocket_server;

import com.cardplatform.card_websocket_server.DTO.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageListener {

    private final WebSocketSessionManager sessionManager;

    @Autowired
    public KafkaMessageListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @KafkaListener(topics = "test-topic", groupId = "websocket-group", containerFactory = "kafkaListenerContainerFactory")
    public void listenAndBroadcast(ChatMessage chatMessage) {
        System.out.println("Kafka로부터 메시지 수신: " + chatMessage.getMessage());
        sessionManager.broadcast("카프카로부터: " + chatMessage.getMessage()).subscribe();
    }
}