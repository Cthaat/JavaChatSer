package com.example.javachat.websocket;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OnlineStatusService {

    private static final Logger log = LoggerFactory.getLogger(OnlineStatusService.class);
    private static final Duration ONLINE_TTL = Duration.ofMinutes(2);

    private final StringRedisTemplate redisTemplate;

    public OnlineStatusService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void markOnline(Long userId, String sessionId) {
        try {
            redisTemplate.opsForValue().set(onlineKey(userId), sessionId, ONLINE_TTL);
        } catch (RuntimeException exception) {
            log.debug("Failed to mark user {} online", userId, exception);
        }
    }

    public void refreshOnline(Long userId, String sessionId) {
        markOnline(userId, sessionId);
    }

    public void markOffline(Long userId) {
        try {
            redisTemplate.delete(onlineKey(userId));
        } catch (RuntimeException exception) {
            log.debug("Failed to mark user {} offline", userId, exception);
        }
    }

    private static String onlineKey(Long userId) {
        return "online:user:" + userId;
    }
}
