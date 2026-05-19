package com.example.javachat.chat.dto;

import com.example.javachat.chat.PrivateMessage;
import java.time.LocalDateTime;

public record PrivateMessageResponse(
        Long id,
        Long senderId,
        Long receiverId,
        String content,
        String messageType,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {

    public static PrivateMessageResponse from(PrivateMessage message) {
        return new PrivateMessageResponse(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent(),
                message.getMessageType().name(),
                message.getReadAt(),
                message.getCreatedAt()
        );
    }
}
