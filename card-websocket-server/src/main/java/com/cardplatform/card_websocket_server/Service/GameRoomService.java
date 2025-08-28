package com.cardplatform.card_websocket_server.Service;

import com.cardplatform.card_websocket_server.DTO.GameRoom;
import com.cardplatform.card_websocket_server.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        return new ArrayList<>(rooms.values());
    }

    public GameRoom createRoom(String title, String password) {
        GameRoom newRoom = new GameRoom(title, password);
        rooms.put(newRoom.getRoomId(), newRoom);
        return newRoom;
    }
}
