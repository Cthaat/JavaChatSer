package com.example.javachat.websocket;

public enum WebSocketMessageType {
    PRIVATE_MESSAGE,
    PUBLIC_MESSAGE,
    FRIEND_REQUEST,
    FRIEND_STATUS,
    MESSAGE_RECALLED,
    ONLINE_USERS,
    PING,
    PONG,
    ERROR
}
