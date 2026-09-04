package com.timofey.shortener_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    @Value("${rate-limit.max-requests}")
    private long maxRequests;

    @Value("${rate-limit.window-seconds}")
    private long windowSeconds;

    private final RedisTemplate<String, Object> redisTemplate;

    public boolean isAllowed(String ip) {
        String key = "rate_limit:" + ip;

        Long requests = redisTemplate.opsForValue().increment(key);

        if (requests == null) {
            return false;
        }

        if (requests == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        return requests <= maxRequests;
    }

}
