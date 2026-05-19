package com.example.javachat.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.javachat.chat.dto.PublicMessageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class PublicChatCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    private ObjectMapper objectMapper;
    private PublicChatCacheService publicChatCacheService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        publicChatCacheService = new PublicChatCacheService(redisTemplate, objectMapper);
    }

    @Test
    void cacheRecentMessageWritesJsonAndKeepsRecentWindow() throws Exception {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        PublicMessageResponse message = new PublicMessageResponse(
                1L,
                7L,
                "hello cached public room",
                "TEXT",
                LocalDateTime.of(2026, 5, 19, 20, 0)
        );

        publicChatCacheService.cacheRecentMessage(message);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(listOperations).rightPush(eq("chat:public:recent"), payloadCaptor.capture());
        verify(listOperations).trim("chat:public:recent", -100, -1);
        verify(redisTemplate).expire("chat:public:recent", Duration.ofDays(1));

        JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
        assertThat(payload.path("senderId").asLong()).isEqualTo(7L);
        assertThat(payload.path("content").asText()).isEqualTo("hello cached public room");
    }
}
