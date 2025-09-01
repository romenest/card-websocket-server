package com.cardplatform.card_websocket_server;

import com.cardplatform.card_websocket_server.DTO.ChatMessage;
import com.cardplatform.card_websocket_server.DTO.UserSessionInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.ToString;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@ToString
public class WebSocketSessionManager {

    @ToString.Exclude
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, UserSessionInfo>> sessionsByRoom = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addSession(WebSocketSession session, String roomId, String senderName) {
        sessionsByRoom.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        sessionsByRoom.get(roomId).put(session.getId(), new UserSessionInfo(session, roomId, senderName));
        System.out.println("새로운 웹소켓 세션이 추가되었습니다. 세션 ID: " + session.getId());

        broadcastRoomUpdate(roomId, "system", String.format("[%s]님이 채널 %s에 입장했습니다.", senderName, roomId));
    }

    public void removeSession(WebSocketSession session) {
        for (Map.Entry<String, ConcurrentHashMap<String, UserSessionInfo>> roomEntry : sessionsByRoom.entrySet()) {
            UserSessionInfo userInfo = roomEntry.getValue().remove(session.getId());
            if (userInfo != null) {
                System.out.println("웹소켓 세션이 제거되었습니다. 세션 ID: " + session.getId());
                broadcastRoomUpdate(userInfo.getRoomId(), "system", String.format("[%s]님이 채널에서 나갔습니다.", userInfo.getSenderName()));
                break;
            }
        }
    }

    public List<String> getUsersInRoom(String roomId) {
        ConcurrentHashMap<String, UserSessionInfo> roomSessions = sessionsByRoom.get(roomId);
        if (roomSessions != null) {
            return roomSessions.values().stream()
                    .map(UserSessionInfo::getSenderName)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public void broadcastToRoom(ChatMessage chatMessage) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(chatMessage);
            Mono.when(sessionsByRoom.getOrDefault(chatMessage.getRoomId(), new ConcurrentHashMap<>()).values().stream()
                    .filter(info -> info.getSession().isOpen())
                    .map(info -> info.getSession().send(Mono.just(info.getSession().textMessage(jsonMessage))))
                    .toArray(Mono[]::new)
            ).subscribe();
        } catch (JsonProcessingException e) {
            System.err.println("JSON 변환 오류: " + e.getMessage());
        }
    }

    // WebSocketSessionManager.java의 broadcastRoomUpdate 메서드
    public void broadcastRoomUpdate(String roomId, String sender, String message) {
        int userCount = 0;
        List<String> users = Collections.emptyList();

        ConcurrentHashMap<String, UserSessionInfo> roomSessions = sessionsByRoom.get(roomId);
        if (roomSessions != null) {
            userCount = roomSessions.size();
            users = getUsersInRoom(roomId);
        }

        // type 필드("system")를 추가하여 5개의 인자를 전달합니다.
        ChatMessage roomUpdateMessage = new ChatMessage("system", sender, message, roomId, System.currentTimeMillis());
        roomUpdateMessage.setUserCount(userCount);
        roomUpdateMessage.setUsersInRoom(users);

        try {
            String jsonMessage = objectMapper.writeValueAsString(roomUpdateMessage);
            Mono.when(roomSessions.values().stream()
                    .filter(info -> info.getSession().isOpen())
                    .map(info -> info.getSession().send(Mono.just(info.getSession().textMessage(jsonMessage))))
                    .toArray(Mono[]::new)
            ).subscribe();
        } catch (JsonProcessingException e) {
            System.err.println("JSON 변환 오류: " + e.getMessage());
        }
    }

    public void broadcastAll(ChatMessage chatMessage) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(chatMessage);
            Mono.when(sessionsByRoom.values().stream()
                    .flatMap(roomSessions -> roomSessions.values().stream())
                    .filter(info -> info.getSession().isOpen())
                    .map(info -> info.getSession().send(Mono.just(info.getSession().textMessage(jsonMessage))))
                    .toArray(Mono[]::new)
            ).subscribe();
        } catch (JsonProcessingException e) {
            System.err.println("JSON 변환 오류: " + e.getMessage());
        }
    }
}