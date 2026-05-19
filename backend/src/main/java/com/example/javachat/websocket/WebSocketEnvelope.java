package com.example.javachat.websocket;

public record WebSocketEnvelope<T>(WebSocketMessageType type, T data) {

    public static <T> WebSocketEnvelope<T> of(WebSocketMessageType type, T data) {
        return new WebSocketEnvelope<>(type, data);
    }
}
