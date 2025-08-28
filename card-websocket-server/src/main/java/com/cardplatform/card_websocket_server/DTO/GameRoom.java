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
    private String password; // null이면 비밀번호 없음
    private int maxPlayers;
    private int currentPlayerCount;

    public GameRoom(String title, String password) {
        this.roomId = UUID.randomUUID().toString(); // 고유한 ID 자동 생성
        this.title = title;
        this.password = password;
        this.maxPlayers = 2; // 일단 2명으로 고정
        this.currentPlayerCount = 0;
    }
}