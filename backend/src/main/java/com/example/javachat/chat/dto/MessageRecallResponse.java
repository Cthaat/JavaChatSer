package com.example.javachat.chat.dto;

import java.time.LocalDateTime;

public record MessageRecallResponse(
        String scope,
        Long messageId,
        Long senderId,
        Long receiverId,
        LocalDateTime recalledAt
) {
}
