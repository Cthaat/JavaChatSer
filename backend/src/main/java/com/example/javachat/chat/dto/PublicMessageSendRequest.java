package com.example.javachat.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicMessageSendRequest(
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 2000, message = "消息内容最多 2000 个字符")
        String content
) {
}
