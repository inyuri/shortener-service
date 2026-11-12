package com.timofey.shortener_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.QueryTimeoutException;
import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    @Value("${rate-limit.max-requests}")
    private long maxRequests;

    @Value("${rate-limit.window-seconds}")
    private long windowSeconds;

    private final RedisTemplate<String, Object> redisTemplate;

    public boolean isAllowed(String ip) {
        try {
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
        catch (RedisConnectionFailureException | QueryTimeoutException e) {
            log.warn("Redis is unavailable");
            return true;
        }
    }

}
