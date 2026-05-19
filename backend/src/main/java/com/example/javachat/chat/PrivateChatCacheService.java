package com.example.javachat.chat;

import com.example.javachat.chat.dto.PrivateMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PrivateChatCacheService {

    private static final Logger log = LoggerFactory.getLogger(PrivateChatCacheService.class);
    private static final Duration RECENT_MESSAGE_TTL = Duration.ofDays(1);
    private static final long RECENT_MESSAGE_LIMIT = 100;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public PrivateChatCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheRecentMessage(Long userId, Long friendId, PrivateMessageResponse message) {
        String key = privateMessageKey(userId, friendId);
        try {
            redisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(message));
            redisTemplate.opsForList().trim(key, -RECENT_MESSAGE_LIMIT, -1);
            redisTemplate.expire(key, RECENT_MESSAGE_TTL);
        } catch (JsonProcessingException | RuntimeException exception) {
            log.debug("Failed to cache private message for users {} and {}", userId, friendId, exception);
        }
    }

    public long getUnreadCount(Long receiverId, Long senderId, LongSupplier fallback) {
        try {
            String value = redisTemplate.opsForValue().get(unreadKey(receiverId, senderId));
            if (value != null && !value.isBlank()) {
                return Long.parseLong(value);
            }
        } catch (RuntimeException exception) {
            log.debug("Failed to read unread count for receiver {} and sender {}", receiverId, senderId, exception);
        }
        long unreadCount = fallback.getAsLong();
        setUnreadCount(receiverId, senderId, unreadCount);
        return unreadCount;
    }

    public void setUnreadCount(Long receiverId, Long senderId, long unreadCount) {
        try {
            if (unreadCount <= 0) {
                redisTemplate.delete(unreadKey(receiverId, senderId));
                return;
            }
            redisTemplate.opsForValue().set(unreadKey(receiverId, senderId), String.valueOf(unreadCount));
        } catch (RuntimeException exception) {
            log.debug("Failed to write unread count for receiver {} and sender {}", receiverId, senderId, exception);
        }
    }

    public void clearUnreadCount(Long receiverId, Long senderId) {
        try {
            redisTemplate.delete(unreadKey(receiverId, senderId));
        } catch (RuntimeException exception) {
            log.debug("Failed to clear unread count for receiver {} and sender {}", receiverId, senderId, exception);
        }
    }

    private static String privateMessageKey(Long userId, Long friendId) {
        long minUserId = Math.min(userId, friendId);
        long maxUserId = Math.max(userId, friendId);
        return "chat:private:" + minUserId + ":" + maxUserId;
    }

    private static String unreadKey(Long receiverId, Long senderId) {
        return "unread:" + receiverId + ":" + senderId;
    }
}
