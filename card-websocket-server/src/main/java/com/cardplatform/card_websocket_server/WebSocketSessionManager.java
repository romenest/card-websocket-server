package com.cardplatform.card_websocket_server;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("새로운 웹소켓 세션이 추가되었습니다. 세션 ID: " + session.getId());
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
        System.out.println("웹소켓 세션이 제거되었습니다. 세션 ID: " + session.getId());
    }

    public Mono<Void> broadcast(String message) {
        return Mono.when(sessions.values().stream()
                .filter(WebSocketSession::isOpen)
                .map(session -> session.send(Mono.just(session.textMessage(message)))
                        .doOnSuccess(aVoid -> System.out.println("메시지 브로드캐스트 성공: " + message))
                        .doOnError(throwable -> System.out.println("메시지 전송 실패: " + throwable.getMessage()))
                )
                .toArray(Mono[]::new));
    }
}