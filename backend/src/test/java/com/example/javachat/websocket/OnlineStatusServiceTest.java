package com.example.javachat.websocket;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class OnlineStatusServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private OnlineStatusService onlineStatusService;

    @BeforeEach
    void setUp() {
        onlineStatusService = new OnlineStatusService(redisTemplate);
    }

    @Test
    void markOnlineWritesSessionIdWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        onlineStatusService.markOnline(7L, "session-7");

        verify(valueOperations).set("online:user:7", "session-7", Duration.ofMinutes(2));
    }

    @Test
    void markOfflineDeletesOnlineKey() {
        onlineStatusService.markOffline(7L);

        verify(redisTemplate).delete("online:user:7");
    }
}
