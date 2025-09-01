package com.cardplatform.card_websocket_server.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom {
    private String roomId;
    private String title;
    private String password;
    private int maxPlayers;
    private int currentPlayerCount;

    public GameRoom(String title, String password) {
        this.roomId = UUID.randomUUID().toString();
        this.title = title;
        this.password = password;
        this.maxPlayers = 2;
        this.currentPlayerCount = 0;
    }
}