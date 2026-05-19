package com.example.javachat.chat;

import com.example.javachat.chat.dto.PublicMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublicChatCacheService {

    private static final Logger log = LoggerFactory.getLogger(PublicChatCacheService.class);
    private static final String PUBLIC_RECENT_KEY = "chat:public:recent";
    private static final Duration RECENT_MESSAGE_TTL = Duration.ofDays(1);
    private static final long RECENT_MESSAGE_LIMIT = 100;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public PublicChatCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheRecentMessage(PublicMessageResponse message) {
        try {
            redisTemplate.opsForList().rightPush(PUBLIC_RECENT_KEY, objectMapper.writeValueAsString(message));
            redisTemplate.opsForList().trim(PUBLIC_RECENT_KEY, -RECENT_MESSAGE_LIMIT, -1);
            redisTemplate.expire(PUBLIC_RECENT_KEY, RECENT_MESSAGE_TTL);
        } catch (JsonProcessingException | RuntimeException exception) {
            log.debug("Failed to cache public message {}", message.id(), exception);
        }
    }
}
