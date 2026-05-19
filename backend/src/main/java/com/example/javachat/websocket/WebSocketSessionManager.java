package com.example.javachat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketSessionManager {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> userIdBySessionId = new ConcurrentHashMap<>();

    public WebSocketSessionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(Long userId, WebSocketSession session) {
        sessionsByUserId.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
        userIdBySessionId.put(session.getId(), userId);
    }

    public boolean unregister(WebSocketSession session) {
        Long userId = userIdBySessionId.remove(session.getId());
        if (userId == null) {
            return false;
        }
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null) {
            return false;
        }
        sessions.remove(session);
        sessions.removeIf(item -> !item.isOpen());
        if (sessions.isEmpty()) {
            sessionsByUserId.remove(userId);
            return true;
        }
        return false;
    }

    public boolean hasOpenSession(Long userId) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null) {
            return false;
        }
        sessions.removeIf(session -> !session.isOpen());
        return !sessions.isEmpty();
    }

    public Set<Long> onlineUserIds() {
        return Set.copyOf(sessionsByUserId.keySet());
    }

    public void sendToUser(Long userId, WebSocketEnvelope<?> envelope) {
        Set<WebSocketSession> sessions = sessionsByUserId.getOrDefault(userId, Collections.emptySet());
        for (WebSocketSession session : new ArrayList<>(sessions)) {
            send(session, envelope);
        }
    }

    public void broadcast(WebSocketEnvelope<?> envelope) {
        for (Set<WebSocketSession> sessions : sessionsByUserId.values()) {
            for (WebSocketSession session : new ArrayList<>(sessions)) {
                send(session, envelope);
            }
        }
    }

    public void send(WebSocketSession session, WebSocketEnvelope<?> envelope) {
        if (!session.isOpen()) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(envelope);
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                }
            }
        } catch (IOException exception) {
            log.debug("Failed to send websocket message to session {}", session.getId(), exception);
        }
    }
}
