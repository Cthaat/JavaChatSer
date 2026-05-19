package com.example.javachat.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

class WebSocketSessionManagerTest {

    @Test
    void hasOpenSessionReturnsFalseForUnknownUser() {
        WebSocketSessionManager sessionManager = new WebSocketSessionManager(new ObjectMapper());

        assertThat(sessionManager.hasOpenSession(99L)).isFalse();
    }

    @Test
    void unregisterReturnsTrueOnlyWhenLastSessionIsRemoved() {
        WebSocketSessionManager sessionManager = new WebSocketSessionManager(new ObjectMapper());
        WebSocketSession firstSession = openSession("first");
        WebSocketSession secondSession = openSession("second");

        sessionManager.register(7L, firstSession);
        sessionManager.register(7L, secondSession);

        assertThat(sessionManager.unregister(firstSession)).isFalse();
        assertThat(sessionManager.hasOpenSession(7L)).isTrue();
        assertThat(sessionManager.unregister(secondSession)).isTrue();
        assertThat(sessionManager.hasOpenSession(7L)).isFalse();
    }

    private static WebSocketSession openSession(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }
}
