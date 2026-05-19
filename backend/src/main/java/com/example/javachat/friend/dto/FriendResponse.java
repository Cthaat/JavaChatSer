package com.example.javachat.friend.dto;

public record FriendResponse(
        FriendUserResponse user,
        boolean online,
        long unreadCount
) {
}
