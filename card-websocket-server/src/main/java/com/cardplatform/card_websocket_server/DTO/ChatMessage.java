package com.cardplatform.card_websocket_server.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String type; // 메시지 타입 (e.g., "chat", "system")
    private String sender;
    private String message;
    private String roomId;
    private long timestamp;
    private int userCount;
    private List<String> usersInRoom;

    // 1. 일반 메시지 생성을 위한 생성자 (클라이언트가 메시지 보낼 때)
    public ChatMessage(String type, String sender, String message, String roomId) {
        this.type = type;
        this.sender = sender;
        this.message = message;
        this.roomId = roomId;
        this.timestamp = System.currentTimeMillis();
        this.userCount = 0;
        this.usersInRoom = new ArrayList<>();
    }

    // 2. 시스템 메시지 생성을 위한 생성자 (서버에서 알림 보낼 때)
    public ChatMessage(String type, String sender, String message, String roomId, long timestamp) {
        this.type = type;
        this.sender = sender;
        this.message = message;
        this.roomId = roomId;
        this.timestamp = timestamp;
        this.userCount = 0;
        this.usersInRoom = new ArrayList<>();
    }
}