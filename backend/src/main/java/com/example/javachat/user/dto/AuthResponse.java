package com.example.javachat.user.dto;

public record AuthResponse(String token, UserProfileResponse user) {
}
