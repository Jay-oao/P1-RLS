package com.p1rls.rls.strategy;

import com.p1rls.rls.model.Algorithm;
import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;
import com.p1rls.rls.utils.RedisExecutorUtil;
import com.p1rls.rls.utils.RedisScriptLoader;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@SuppressWarnings("unchecked")
public class FixedWindowStrategy implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;

    private final RedisScriptLoader scriptLoader;

    private final RedisExecutorUtil redisExecutor;

    private DefaultRedisScript<List> fixedWindowScript;

    public FixedWindowStrategy(StringRedisTemplate redisTemplate, RedisScriptLoader scriptLoader, RedisExecutorUtil redisExecutor) {
        this.redisTemplate = redisTemplate;
        this.scriptLoader = scriptLoader;
        this.redisExecutor = redisExecutor;
    }

    @PostConstruct
    public void init() {
        fixedWindowScript = scriptLoader.load("scripts/fixed_window.lua");
    }

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.FIXED_WINDOW;
    }

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