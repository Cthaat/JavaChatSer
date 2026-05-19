package com.example.javachat.websocket;

import com.example.javachat.chat.ChatService;
import com.example.javachat.chat.dto.PrivateMessageResponse;
import com.example.javachat.chat.dto.PrivateMessageSendRequest;
import com.example.javachat.chat.dto.PublicMessageResponse;
import com.example.javachat.common.BusinessException;
import com.example.javachat.common.ErrorCode;
import com.example.javachat.friend.FriendRelation;
import com.example.javachat.friend.FriendRepository;
import com.example.javachat.friend.FriendStatus;
import com.example.javachat.security.LoginUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final FriendRepository friendRepository;
    private final WebSocketSessionManager sessionManager;
    private final OnlineStatusService onlineStatusService;
    private final ChatRealtimeNotifier chatRealtimeNotifier;

    public ChatWebSocketHandler(
            ObjectMapper objectMapper,
            ChatService chatService,
            FriendRepository friendRepository,
            WebSocketSessionManager sessionManager,
            OnlineStatusService onlineStatusService,
            ChatRealtimeNotifier chatRealtimeNotifier
    ) {
        this.objectMapper = objectMapper;
        this.chatService = chatService;
        this.friendRepository = friendRepository;
        this.sessionManager = sessionManager;
        this.onlineStatusService = onlineStatusService;
        this.chatRealtimeNotifier = chatRealtimeNotifier;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LoginUser loginUser = loginUser(session);
        sessionManager.register(loginUser.id(), session);
        onlineStatusService.markOnline(loginUser.id(), session.getId());
        sessionManager.send(
                session,
                WebSocketEnvelope.of(WebSocketMessageType.ONLINE_USERS, sessionManager.onlineUserIds())
        );
        notifyFriends(loginUser.id(), true);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        LoginUser loginUser = loginUser(session);

        try {
            onlineStatusService.refreshOnline(loginUser.id(), session.getId());
            JsonNode payload = objectMapper.readTree(message.getPayload());
            WebSocketMessageType type = parseType(payload);
            switch (type) {
                case PRIVATE_MESSAGE -> handlePrivateMessage(loginUser, payload);
                case PUBLIC_MESSAGE -> handlePublicMessage(loginUser, payload);
                case PING -> sessionManager.send(session, WebSocketEnvelope.of(WebSocketMessageType.PONG, null));
                default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的 WebSocket 消息类型");
            }
        } catch (BusinessException exception) {
            sessionManager.send(session, WebSocketEnvelope.of(
                    WebSocketMessageType.ERROR,
                    new WebSocketErrorResponse(exception.getErrorCode().code(), exception.getMessage())
            ));
        } catch (JsonProcessingException exception) {
            sessionManager.send(session, WebSocketEnvelope.of(
                    WebSocketMessageType.ERROR,
                    new WebSocketErrorResponse(ErrorCode.BAD_REQUEST.code(), "WebSocket 消息格式错误")
            ));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LoginUser loginUser = loginUserOrNull(session);
        boolean userFullyOffline = sessionManager.unregister(session);
        if (loginUser != null && userFullyOffline) {
            onlineStatusService.markOffline(loginUser.id());
            notifyFriends(loginUser.id(), false);
        }
    }

    private void handlePrivateMessage(LoginUser loginUser, JsonNode payload) {
        Long receiverId = requiredLong(payload, "receiverId");
        String content = requiredText(payload, "content");
        PrivateMessageResponse response = chatService.sendPrivateMessage(
                loginUser.id(),
                receiverId,
                new PrivateMessageSendRequest(content, optionalText(payload, "messageType"))
        );
        WebSocketEnvelope<PrivateMessageResponse> envelope = WebSocketEnvelope.of(
                WebSocketMessageType.PRIVATE_MESSAGE,
                response
        );
        sessionManager.sendToUser(loginUser.id(), envelope);
        sessionManager.sendToUser(receiverId, envelope);
    }

    private void handlePublicMessage(LoginUser loginUser, JsonNode payload) {
        String content = requiredText(payload, "content");
        PublicMessageResponse response = chatService.sendPublicMessage(
                loginUser.id(),
                content,
                optionalText(payload, "messageType")
        );
        chatRealtimeNotifier.broadcastPublicMessage(response);
    }

    private void notifyFriends(Long userId, boolean online) {
        List<Long> friendIds = friendRepository.findByUserIdAndStatus(userId, FriendStatus.ACCEPTED)
                .stream()
                .map(FriendRelation::getFriendId)
                .toList();
        WebSocketEnvelope<FriendStatusMessage> envelope = WebSocketEnvelope.of(
                WebSocketMessageType.FRIEND_STATUS,
                new FriendStatusMessage(userId, online)
        );
        for (Long friendId : friendIds) {
            sessionManager.sendToUser(friendId, envelope);
        }
    }

    private static LoginUser loginUser(WebSocketSession session) {
        Object value = session.getAttributes().get(WebSocketAttributes.LOGIN_USER);
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    private static LoginUser loginUserOrNull(WebSocketSession session) {
        Object value = session.getAttributes().get(WebSocketAttributes.LOGIN_USER);
        return value instanceof LoginUser loginUser ? loginUser : null;
    }

    private static WebSocketMessageType parseType(JsonNode payload) {
        String type = requiredText(payload, "type");
        try {
            return WebSocketMessageType.valueOf(type);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的 WebSocket 消息类型");
        }
    }

    private static Long requiredLong(JsonNode payload, String fieldName) {
        JsonNode value = payload.get(fieldName);
        if (value == null || !value.canConvertToLong()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "缺少必要字段: " + fieldName);
        }
        return value.asLong();
    }

    private static String requiredText(JsonNode payload, String fieldName) {
        JsonNode value = payload.get(fieldName);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "缺少必要字段: " + fieldName);
        }
        return value.asText();
    }

    private static String optionalText(JsonNode payload, String fieldName) {
        JsonNode value = payload.get(fieldName);
        return value != null && value.isTextual() ? value.asText() : null;
    }
}
