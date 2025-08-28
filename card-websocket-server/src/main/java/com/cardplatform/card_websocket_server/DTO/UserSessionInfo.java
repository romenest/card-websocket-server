package com.cardplatform.card_websocket_server.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.reactive.socket.WebSocketSession;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionInfo {
    @ToString.Exclude
    private WebSocketSession session;
    private String roomId;
    private String senderName;
}