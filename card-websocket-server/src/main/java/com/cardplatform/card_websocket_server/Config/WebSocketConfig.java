package com.cardplatform.card_websocket_server.Config;

import com.cardplatform.card_websocket_server.DTO.ChatMessage;
import com.cardplatform.card_websocket_server.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebSocketConfig(WebSocketSessionManager sessionManager, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    @Bean
    public HandlerMapping webSocketMapping(WebSocketHandler myHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/game", myHandler);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public WebSocketHandler myHandler(KafkaTemplate<String, ChatMessage> kafkaTemplate) {
        return session -> {
            sessionManager.addSession(session);
            return session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .flatMap(msg -> {
                        try {
                            ChatMessage chatMessage = objectMapper.readValue(msg, ChatMessage.class);
                            kafkaTemplate.send("test-topic", chatMessage);
                            String responseMsg = "Echo: " + chatMessage.getMessage();
                            return session.send(Mono.just(session.textMessage(responseMsg)));
                        } catch (Exception e) {
                            System.err.println("JSON 파싱 오류: " + e.getMessage());
                            return Mono.error(e);
                        }
                    })
                    .doFinally(signalType -> sessionManager.removeSession(session))
                    .then();
        };
    }
}