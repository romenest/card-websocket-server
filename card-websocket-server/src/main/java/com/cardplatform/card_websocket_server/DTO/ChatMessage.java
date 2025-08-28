package com.cardplatform.card_websocket_server.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sender;
    private String message;
    private String roomId; // String으로 변경
    private long timestamp;
    private int userCount;
    private List<String> usersInRoom;

    public ChatMessage(String sender, String message, long l) {
        this.sender = sender;
        this.message = message;
        this.timestamp = l;
    }
}