package com.example.javachat.websocket;

import com.example.javachat.chat.dto.PublicMessageResponse;
import com.example.javachat.chat.dto.MessageRecallResponse;
import com.example.javachat.friend.dto.FriendRequestResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatRealtimeNotifier {

    private final WebSocketSessionManager sessionManager;

    public ChatRealtimeNotifier(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void broadcastPublicMessage(PublicMessageResponse message) {
        sessionManager.broadcast(WebSocketEnvelope.of(WebSocketMessageType.PUBLIC_MESSAGE, message));
    }

    public void notifyFriendRequest(Long receiverId, FriendRequestResponse request) {
        sessionManager.sendToUser(receiverId, WebSocketEnvelope.of(WebSocketMessageType.FRIEND_REQUEST, request));
    }

    public void notifyPrivateMessageRecall(MessageRecallResponse recall) {
        WebSocketEnvelope<MessageRecallResponse> envelope = WebSocketEnvelope.of(
                WebSocketMessageType.MESSAGE_RECALLED,
                recall
        );
        sessionManager.sendToUser(recall.senderId(), envelope);
        if (recall.receiverId() != null) {
            sessionManager.sendToUser(recall.receiverId(), envelope);
        }
    }

    public void broadcastMessageRecall(MessageRecallResponse recall) {
        sessionManager.broadcast(WebSocketEnvelope.of(WebSocketMessageType.MESSAGE_RECALLED, recall));
    }
}
