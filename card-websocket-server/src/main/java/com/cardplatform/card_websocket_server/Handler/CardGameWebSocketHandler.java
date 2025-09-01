package com.cardplatform.card_websocket_server.Handler;

import com.cardplatform.card_websocket_server.DTO.ChatMessage;
import com.cardplatform.card_websocket_server.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class CardGameWebSocketHandler implements WebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public CardGameWebSocketHandler(WebSocketSessionManager sessionManager, KafkaTemplate<String, ChatMessage> kafkaTemplate, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(msg -> {
                    try {
                        ChatMessage chatMessage = objectMapper.readValue(msg, ChatMessage.class);

                        if ("system".equals(chatMessage.getType()) && chatMessage.getMessage().contains("입장했습니다.")) {
                            sessionManager.addSession(session, chatMessage.getRoomId(), chatMessage.getSender());
                        } else {
                            kafkaTemplate.send("game-chat-topic", chatMessage);
                        }

                        return Mono.empty();

                    } catch (Exception e) {
                        System.err.println("JSON 파싱 오류: " + e.getMessage());
                        return Mono.error(e);
                    }
                })
                .doFinally(signalType -> {
                    sessionManager.removeSession(session);
                })
                .then();
    }
}