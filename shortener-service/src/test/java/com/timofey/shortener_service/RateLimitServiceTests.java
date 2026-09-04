package com.timofey.shortener_service;

import com.timofey.shortener_service.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTests {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        ReflectionTestUtils.setField(
                rateLimitService,
                "maxRequests",
                10L
        );

        ReflectionTestUtils.setField(
                rateLimitService,
                "windowSeconds",
                60L
        );
    }

    @Test
    void firstRequestShouldBeAllowed() {
        when(valueOperations.increment("rate_limit:127.0.0.1"))
                .thenReturn(1L);

        boolean result = rateLimitService.isAllowed("127.0.0.1");

        assertTrue(result);
    }

    @Test
    void eleventhRequestShouldBeBlocked() {
        when(valueOperations.increment("rate_limit:127.0.0.1"))
                .thenReturn(11L);

        boolean result = rateLimitService.isAllowed("127.0.0.1");

        assertFalse(result);
    }

    @Test
    void firstRequestShouldSetTtl() {
        when(valueOperations.increment("rate_limit:127.0.0.1"))
                .thenReturn(1L);

        rateLimitService.isAllowed("127.0.0.1");

        verify(redisTemplate)
                .expire(
                        "rate_limit:127.0.0.1",
                        Duration.ofSeconds(60)
                );
    }

    @Test
    void nextRequestsShouldNotResetTtl() {
        when(valueOperations.increment("rate_limit:127.0.0.1"))
                .thenReturn(2L);

        rateLimitService.isAllowed("127.0.0.1");

        verify(redisTemplate, never())
                .expire(
                        anyString(),
                        any(Duration.class)
                );
    }
}