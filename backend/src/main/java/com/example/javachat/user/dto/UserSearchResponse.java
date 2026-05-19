package com.example.javachat.user.dto;

import com.example.javachat.user.User;

public record UserSearchResponse(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        String bio
) {

    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getBio()
        );
    }
}
