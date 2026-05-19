package com.example.javachat.websocket;

public enum WebSocketMessageType {
    PRIVATE_MESSAGE,
    PUBLIC_MESSAGE,
    FRIEND_STATUS,
    ONLINE_USERS,
    PING,
    PONG,
    ERROR
}
