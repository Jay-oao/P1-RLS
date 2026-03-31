package com.p1rls.rls.strategy;

import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

// Can crash due to race condition, crash before setting TTL
@Component
public class FixedWindowStrategy implements RateLimiterStrategy {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public RLSResponse allowRequest(RLSRequest request) {

        String key = request.getKey();
        int limit = request.getPolicy().getLimit();
        int windowSeconds = request.getPolicy().getWindowSeconds();
        long now = request.getTimestamp();

        long windowSize = windowSeconds * 1000L;
        long windowStart = (now / windowSize) * windowSize;
        long windowEnd = windowStart + windowSize;

        String redisKey = key + ":" + windowStart;

        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }

        if (count == null) {
            return RLSResponse.builder()
                    .allowed(true)
                    .remaining(limit)
                    .retryAfterMs(0)
                    .resetTime(windowEnd)
                    .message("Allowed")
                    .build();
        }

        if (count <= limit) {
            return RLSResponse.builder()
                    .allowed(true)
                    .remaining(limit - count.intValue())
                    .retryAfterMs(0)
                    .resetTime(windowEnd)
                    .message("Allowed")
                    .build();
        }

        long retryAfter = windowEnd - now;

        return RLSResponse.builder()
                .allowed(false)
                .remaining(0)
                .retryAfterMs(retryAfter)
                .resetTime(windowEnd)
                .message("Rate limit exceeded")
                .build();
    }
}