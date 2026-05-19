package com.example.javachat.websocket;

import com.example.javachat.chat.dto.PublicMessageResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatRealtimeNotifier {

    private final WebSocketSessionManager sessionManager;

    public ChatRealtimeNotifier(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void broadcastPublicMessage(PublicMessageResponse message) {
        sessionManager.broadcast(WebSocketEnvelope.of(WebSocketMessageType.PUBLIC_MESSAGE, message));
    }
}
