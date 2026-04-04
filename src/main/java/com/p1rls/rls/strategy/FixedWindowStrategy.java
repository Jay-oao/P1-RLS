package com.p1rls.rls.strategy;

import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;
import com.p1rls.rls.utils.RedisExecutorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class FixedWindowStrategy implements RateLimiterStrategy {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DefaultRedisScript<List> fixedWindowScript;

    @Autowired
    private RedisExecutorUtil redisExecutor;

    @Override
    public RLSResponse allowRequest(RLSRequest request) {

        String key = request.getKey();
        long limit = request.getPolicy().getLimit();
        long windowSeconds = request.getPolicy().getWindowSeconds();
        long now = request.getTimestamp();

        long windowSizeMs = windowSeconds * 1000L;
        long windowStart = (now / windowSizeMs) * windowSizeMs;
        long windowEnd = windowStart + windowSizeMs;

        String redisKey = key + ":" + windowStart;

        return redisExecutor.execute(

                () -> {

                    List<Long> result = redisTemplate.execute(
                            fixedWindowScript,
                            Collections.singletonList(redisKey),
                            String.valueOf(windowSeconds)
                    );

                    if (result == null || result.size() < 2) {
                        return fallback(limit, windowEnd);
                    }

                    long current = result.get(0);
                    long ttlSeconds = result.get(1);

                    long retryAfterMs = ttlSeconds * 1000;

                    if (current <= limit) {
                        return RLSResponse.builder()
                                .allowed(true)
                                .remaining(limit - current)
                                .retryAfterMs(0)
                                .resetTime(windowEnd)
                                .message("Allowed")
                                .build();
                    }

                    return RLSResponse.builder()
                            .allowed(false)
                            .remaining(0)
                            .retryAfterMs(retryAfterMs)
                            .resetTime(windowEnd)
                            .message("Rate limit exceeded")
                            .build();
                },

                () -> fallback(limit, windowEnd)
        );
    }

    private RLSResponse fallback(long limit, long windowEnd) {
        return RLSResponse.builder()
                .allowed(true)
                .remaining(limit)
                .retryAfterMs(0)
                .resetTime(windowEnd)
                .message("Allowed (Redis failure fallback)")
                .build();
    }
}