package com.example.javachat.friend;

import com.example.javachat.friend.dto.FriendUserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FriendCacheService {

    private static final Logger log = LoggerFactory.getLogger(FriendCacheService.class);
    private static final Duration FRIEND_LIST_TTL = Duration.ofMinutes(5);
    private static final TypeReference<List<FriendUserResponse>> FRIEND_LIST_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public FriendCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<List<FriendUserResponse>> getFriendUsers(Long userId) {
        try {
            String cachedValue = redisTemplate.opsForValue().get(friendListKey(userId));
            if (cachedValue == null || cachedValue.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cachedValue, FRIEND_LIST_TYPE));
        } catch (JsonProcessingException | RedisConnectionFailureException exception) {
            log.debug("Failed to read friend list cache for user {}", userId, exception);
            return Optional.empty();
        }
    }

    public void putFriendUsers(Long userId, List<FriendUserResponse> friends) {
        try {
            redisTemplate.opsForValue().set(
                    friendListKey(userId),
                    objectMapper.writeValueAsString(friends),
                    FRIEND_LIST_TTL
            );
        } catch (JsonProcessingException | RedisConnectionFailureException exception) {
            log.debug("Failed to write friend list cache for user {}", userId, exception);
        }
    }

    public boolean isOnline(Long userId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(onlineKey(userId)));
        } catch (RedisConnectionFailureException exception) {
            log.debug("Failed to read online status for user {}", userId, exception);
            return false;
        }
    }

    public void evictFriendList(Long userId) {
        try {
            redisTemplate.delete(friendListKey(userId));
        } catch (RedisConnectionFailureException exception) {
            log.debug("Failed to evict friend list cache for user {}", userId, exception);
        }
    }

    private static String friendListKey(Long userId) {
        return "friend:list:" + userId;
    }

    private static String onlineKey(Long userId) {
        return "online:user:" + userId;
    }
}
