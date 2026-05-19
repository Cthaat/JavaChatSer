package com.example.javachat.user.dto;

import com.example.javachat.security.LoginUser;
import com.example.javachat.user.User;

public record UserProfileResponse(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        String bio,
        String role
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getRole().name()
        );
    }

    public static UserProfileResponse from(LoginUser user) {
        return new UserProfileResponse(
                user.id(),
                user.username(),
                user.nickname(),
                user.avatarUrl(),
                user.bio(),
                user.role().name()
        );
    }
}
