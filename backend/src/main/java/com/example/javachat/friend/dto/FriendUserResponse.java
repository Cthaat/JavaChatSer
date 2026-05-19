package com.example.javachat.friend.dto;

import com.example.javachat.user.User;

public record FriendUserResponse(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        String bio
) {

    public static FriendUserResponse from(User user) {
        return new FriendUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getBio()
        );
    }
}
