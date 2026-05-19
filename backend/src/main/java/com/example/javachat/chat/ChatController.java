package com.example.javachat.chat;

import com.example.javachat.chat.dto.PrivateMessageResponse;
import com.example.javachat.chat.dto.PrivateMessageSendRequest;
import com.example.javachat.chat.dto.ReadReceiptResponse;
import com.example.javachat.common.ApiResponse;
import com.example.javachat.common.PageResponse;
import com.example.javachat.security.LoginUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/private/{friendId}/messages")
    public ApiResponse<PageResponse<PrivateMessageResponse>> listPrivateMessages(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable @Positive(message = "好友 ID 必须为正数") Long friendId,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "页码不能小于 0")
            int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "每页数量不能小于 1")
            @Max(value = 100, message = "每页数量不能超过 100")
            int size
    ) {
        return ApiResponse.success(chatService.listPrivateMessages(
                loginUser.id(),
                friendId,
                PageRequest.of(page, size)
        ));
    }

    @PostMapping("/private/{friendId}/messages")
    public ApiResponse<PrivateMessageResponse> sendPrivateMessage(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable @Positive(message = "好友 ID 必须为正数") Long friendId,
            @Valid @RequestBody PrivateMessageSendRequest request
    ) {
        return ApiResponse.success(chatService.sendPrivateMessage(loginUser.id(), friendId, request));
    }

    @PostMapping("/private/{friendId}/read")
    public ApiResponse<ReadReceiptResponse> markPrivateMessagesAsRead(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable @Positive(message = "好友 ID 必须为正数") Long friendId
    ) {
        return ApiResponse.success(chatService.markPrivateMessagesAsRead(loginUser.id(), friendId));
    }
}
