package com.example.javachat.chat.dto;

import com.example.javachat.chat.PublicMessage;
import java.time.LocalDateTime;

public record PublicMessageResponse(
        Long id,
        Long senderId,
        String content,
        String messageType,
        boolean recalled,
        LocalDateTime recalledAt,
        LocalDateTime createdAt
) {

    public static PublicMessageResponse from(PublicMessage message) {
        return new PublicMessageResponse(
                message.getId(),
                message.getSenderId(),
                message.isRecalled() ? "" : message.getContent(),
                message.getMessageType().name(),
                message.isRecalled(),
                message.getRecalledAt(),
                message.getCreatedAt()
        );
    }
}
