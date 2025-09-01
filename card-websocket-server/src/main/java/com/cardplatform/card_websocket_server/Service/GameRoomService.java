package com.cardplatform.card_websocket_server.Service;

import com.cardplatform.card_websocket_server.DTO.ChatMessage;
import com.cardplatform.card_websocket_server.DTO.GameRoom;
import com.cardplatform.card_websocket_server.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GameRoomService {

    private final WebSocketSessionManager sessionManager;
    private final ConcurrentHashMap<String, GameRoom> rooms = new ConcurrentHashMap<>();

    @Autowired
    public GameRoomService(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public List<GameRoom> getRooms() {
        return rooms.values().stream().collect(Collectors.toList());
    }

    // GameRoomService.java의 createRoom 메서드
    public GameRoom createRoom(String title, String password) {
        GameRoom newRoom = new GameRoom(title, password);
        rooms.put(newRoom.getRoomId(), newRoom);

        // type 필드("system")를 추가하여 5개의 인자를 전달합니다.
        ChatMessage newRoomMessage = new ChatMessage("system", "system", "새로운 게임방이 생성되었습니다: " + title, "lobby", System.currentTimeMillis());
        sessionManager.broadcastAll(newRoomMessage);
        return newRoom;
    }
}