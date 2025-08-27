package com.cardplatform.card_websocket_server;

import com.cardplatform.card_websocket_server.DTO.ChatMessage;
import com.cardplatform.card_websocket_server.DTO.UserSessionInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class WebSocketSessionManager {

    // roomId별로 UserSessionInfo를 저장하는 중첩 맵
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, UserSessionInfo>> sessionsByRoom = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addSession(WebSocketSession session, int roomId, String senderName) {
        sessionsByRoom.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
        sessionsByRoom.get(roomId).put(session.getId(), new UserSessionInfo(session, roomId, senderName));
        System.out.println("새로운 웹소켓 세션이 추가되었습니다. 세션 ID: " + session.getId());

        broadcastRoomUpdate(roomId, "system", String.format("[%s]님이 채널 %d에 입장했습니다.", senderName, roomId));
    }

    public void removeSession(WebSocketSession session) {
        for (Map.Entry<Integer, ConcurrentHashMap<String, UserSessionInfo>> roomEntry : sessionsByRoom.entrySet()) {
            UserSessionInfo userInfo = roomEntry.getValue().remove(session.getId());
            if (userInfo != null) {
                System.out.println("웹소켓 세션이 제거되었습니다. 세션 ID: " + session.getId());
                broadcastRoomUpdate(userInfo.getRoomId(), "system", String.format("[%s]님이 채널에서 나갔습니다.", userInfo.getSenderName()));
                break;
            }
        }
    }

    // 특정 방에 속한 모든 사용자 목록을 반환하는 메서드
    public List<String> getUsersInRoom(int roomId) {
        ConcurrentHashMap<String, UserSessionInfo> roomSessions = sessionsByRoom.get(roomId);
        if (roomSessions != null) {
            return roomSessions.values().stream()
                    .map(UserSessionInfo::getSenderName)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // 특정 방에만 메시지를 브로드캐스트하는 메서드 (채팅 메시지용)
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

    // 방의 상태 업데이트 메시지를 브로드캐스트하는 메서드 (입/퇴장 알림, 사용자 수)
    public void broadcastRoomUpdate(int roomId, String sender, String message) {
        int userCount = 0;
        List<String> users = Collections.emptyList();

        ConcurrentHashMap<String, UserSessionInfo> roomSessions = sessionsByRoom.get(roomId);
        if (roomSessions != null) {
            userCount = roomSessions.size();
            users = getUsersInRoom(roomId);
        }

        // 새로운 메시지 타입으로 클라이언트에 정보 전송
        ChatMessage roomUpdateMessage = new ChatMessage(sender, message, roomId, System.currentTimeMillis());
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

}