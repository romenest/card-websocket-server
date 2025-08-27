package com.cardplatform.card_websocket_server.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sender;
    private String message;
    private int roomId;
    private long timestamp;
    private int userCount;
    private List<String> usersInRoom;

    // ... 오류 해결을 위해 생성자를 명시적으로 추가합니다.
    public ChatMessage(String sender, String message, int roomId, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.roomId = roomId;
    }

}