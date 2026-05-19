package com.example.javachat.chat;

import com.example.javachat.chat.dto.PrivateMessageResponse;
import com.example.javachat.chat.dto.PrivateMessageSendRequest;
import com.example.javachat.chat.dto.MessageRecallResponse;
import com.example.javachat.chat.dto.PublicMessageResponse;
import com.example.javachat.chat.dto.ReadReceiptResponse;
import com.example.javachat.common.BusinessException;
import com.example.javachat.common.ErrorCode;
import com.example.javachat.common.PageResponse;
import com.example.javachat.friend.FriendRepository;
import com.example.javachat.friend.FriendStatus;
import com.example.javachat.user.UserRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final PrivateMessageRepository privateMessageRepository;
    private final PublicMessageRepository publicMessageRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final PrivateChatCacheService privateChatCacheService;
    private final PublicChatCacheService publicChatCacheService;

    public ChatService(
            PrivateMessageRepository privateMessageRepository,
            PublicMessageRepository publicMessageRepository,
            FriendRepository friendRepository,
            UserRepository userRepository,
            PrivateChatCacheService privateChatCacheService,
            PublicChatCacheService publicChatCacheService
    ) {
        this.privateMessageRepository = privateMessageRepository;
        this.publicMessageRepository = publicMessageRepository;
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.privateChatCacheService = privateChatCacheService;
        this.publicChatCacheService = publicChatCacheService;
    }

    @Transactional(readOnly = true)
    public PageResponse<PrivateMessageResponse> listPrivateMessages(Long userId, Long friendId, Pageable pageable) {
        ensurePrivateChatAllowed(userId, friendId);
        return PageResponse.from(privateMessageRepository
                .findConversation(userId, friendId, pageable)
                .map(PrivateMessageResponse::from));
    }

    @Transactional
    public PrivateMessageResponse sendPrivateMessage(
            Long senderId,
            Long receiverId,
            PrivateMessageSendRequest request
    ) {
        ensurePrivateChatAllowed(senderId, receiverId);
        MessageType messageType = parseMessageType(request.messageType());
        validateMessageContent(request.content(), messageType);
        PrivateMessage message = new PrivateMessage(senderId, receiverId, request.content().trim(), messageType);
        PrivateMessage savedMessage = privateMessageRepository.saveAndFlush(message);
        PrivateMessageResponse response = PrivateMessageResponse.from(savedMessage);

        privateChatCacheService.cacheRecentMessage(senderId, receiverId, response);
        long unreadCount = privateMessageRepository.countByReceiverIdAndSenderIdAndReadAtIsNull(receiverId, senderId);
        privateChatCacheService.setUnreadCount(receiverId, senderId, unreadCount);
        return response;
    }

    @Transactional
    public ReadReceiptResponse markPrivateMessagesAsRead(Long userId, Long friendId) {
        ensurePrivateChatAllowed(userId, friendId);
        int readCount = privateMessageRepository.markMessagesAsRead(userId, friendId, LocalDateTime.now());
        privateChatCacheService.clearUnreadCount(userId, friendId);
        return new ReadReceiptResponse(readCount, 0);
    }

    @Transactional(readOnly = true)
    public PageResponse<PublicMessageResponse> listPublicMessages(Pageable pageable) {
        return PageResponse.from(publicMessageRepository
                .findAllByOrderByCreatedAtAsc(pageable)
                .map(PublicMessageResponse::from));
    }

    @Transactional
    public PublicMessageResponse sendPublicMessage(Long senderId, String content) {
        return sendPublicMessage(senderId, content, null);
    }

    @Transactional
    public PublicMessageResponse sendPublicMessage(Long senderId, String content, String messageTypeValue) {
        MessageType messageType = parseMessageType(messageTypeValue);
        validateMessageContent(content, messageType);
        if (userRepository.findByIdAndEnabledTrue(senderId).isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        PublicMessage savedMessage = publicMessageRepository.saveAndFlush(
                new PublicMessage(senderId, content.trim(), messageType)
        );
        PublicMessageResponse response = PublicMessageResponse.from(savedMessage);
        publicChatCacheService.cacheRecentMessage(response);
        return response;
    }

    @Transactional
    public MessageRecallResponse recallPrivateMessage(Long requesterId, boolean admin, Long messageId) {
        PrivateMessage message = privateMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "消息不存在"));
        if (!admin && !message.getSenderId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能撤回自己发送的消息");
        }
        LocalDateTime recalledAt = message.getRecalledAt();
        if (recalledAt == null) {
            recalledAt = LocalDateTime.now();
            message.recall(recalledAt);
            privateMessageRepository.save(message);
        }
        return new MessageRecallResponse(
                "PRIVATE",
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                recalledAt
        );
    }

    @Transactional
    public MessageRecallResponse recallPublicMessage(Long requesterId, boolean admin, Long messageId) {
        PublicMessage message = publicMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "消息不存在"));
        if (!admin && !message.getSenderId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能撤回自己发送的消息");
        }
        LocalDateTime recalledAt = message.getRecalledAt();
        if (recalledAt == null) {
            recalledAt = LocalDateTime.now();
            message.recall(recalledAt);
            publicMessageRepository.save(message);
        }
        return new MessageRecallResponse(
                "PUBLIC",
                message.getId(),
                message.getSenderId(),
                null,
                recalledAt
        );
    }

    private static MessageType parseMessageType(String value) {
        if (value == null || value.isBlank()) {
            return MessageType.TEXT;
        }
        try {
            return MessageType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的消息类型");
        }
    }

    private static void validateMessageContent(String content, MessageType messageType) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息内容不能为空");
        }
        String normalizedContent = content.trim();
        if (normalizedContent.length() > 2000) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息内容最多 2000 个字符");
        }
        if (messageType == MessageType.IMAGE && !isImageUrl(normalizedContent)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片消息必须使用已上传的图片地址");
        }
    }

    private static boolean isImageUrl(String content) {
        return content.startsWith("/uploads/images/")
                || content.startsWith("http://")
                || content.startsWith("https://");
    }

    private void ensurePrivateChatAllowed(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能和自己发起私聊");
        }
        if (userRepository.findByIdAndEnabledTrue(friendId).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        boolean accepted = friendRepository.existsByUserIdAndFriendIdAndStatus(
                userId,
                friendId,
                FriendStatus.ACCEPTED
        );
        if (!accepted) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "非好友不能发送私聊消息");
        }
    }
}
