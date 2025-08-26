package com.cardplatform.card_websocket_server.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.reactive.socket.WebSocketSession;

@Getter
@Setter
@NoArgsConstructor
public class UserSessionInfo {
    private WebSocketSession session;
    private int roomId;
    private String senderName;

    public UserSessionInfo(WebSocketSession session, int roomId, String senderName) {
        this.session = session;
        this.roomId = roomId;
        this.senderName = senderName;
    }

    public WebSocketSession getSession() { return session; }
    public int getRoomId() { return roomId; }
    public String getSenderName() { return senderName; }
}