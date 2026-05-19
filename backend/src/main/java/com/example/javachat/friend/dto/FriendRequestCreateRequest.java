package com.example.javachat.friend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FriendRequestCreateRequest(
        @NotNull(message = "目标用户不能为空")
        @Positive(message = "目标用户 ID 必须为正数")
        Long friendId
) {
}
