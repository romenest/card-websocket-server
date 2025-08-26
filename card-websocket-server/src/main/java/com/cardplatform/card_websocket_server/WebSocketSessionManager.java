package com.cardplatform.card_websocket_server;

import com.cardplatform.card_websocket_server.DTO.ChatMessage;
import com.cardplatform.card_websocket_server.DTO.UserSessionInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, UserSessionInfo> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addSession(WebSocketSession session, int roomId, String senderName) {
        sessions.put(session.getId(), new UserSessionInfo(session, roomId, senderName));
        System.out.println("새로운 웹소켓 세션이 추가되었습니다. 세션 ID: " + session.getId());

        broadcastToRoom(roomId, "system", String.format("[%s]님이 채널 %d에 입장했습니다.", senderName, roomId));
    }

    public void removeSession(WebSocketSession session) {
        UserSessionInfo userInfo = sessions.remove(session.getId());
        if (userInfo != null) {
            System.out.println("웹소켓 세션이 제거되었습니다. 세션 ID: " + session.getId());
            broadcastToRoom(userInfo.getRoomId(), "system", String.format("[%s]님이 채널에서 나갔습니다.", userInfo.getSenderName()));
        }
    }

    public void broadcastToRoom(int roomId, String sender, String message) {
        ChatMessage chatMessage = new ChatMessage(sender, message, System.currentTimeMillis());

        try {
            String jsonMessage = objectMapper.writeValueAsString(chatMessage);
            Mono.when(sessions.values().stream()
                    .filter(info -> info.getRoomId() == roomId && info.getSession().isOpen())
                    .map(info -> info.getSession().send(Mono.just(info.getSession().textMessage(jsonMessage))))
                    .toArray(Mono[]::new)
            ).subscribe();
        } catch (JsonProcessingException e) {
            System.err.println("JSON 변환 오류: " + e.getMessage());
        }
    }
}