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

import static com.p1rls.rls.utils.RedisFailureMethod.fallbackMethod;

@Component
@SuppressWarnings("unchecked")
public class SlidingWindowLogStrategy implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;

    private final RedisScriptLoader scriptLoader;

    private final RedisExecutorUtil redisExecutor;

    private DefaultRedisScript<List> slidingWindowLogScript;

    public SlidingWindowLogStrategy(StringRedisTemplate redisTemplate, RedisScriptLoader scriptLoader, RedisExecutorUtil redisExecutor) {
        this.redisTemplate = redisTemplate;
        this.scriptLoader = scriptLoader;
        this.redisExecutor = redisExecutor;
    }

    @PostConstruct
    public void init() {
        slidingWindowLogScript = scriptLoader.load("scripts/sliding_window_log.lua");
    }

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.SLIDING_WINDOW_LOG;
    }

    @Override
    public RLSResponse allowRequest(RLSRequest request) {

        String key = request.getKey();
        long limit = request.getPolicy().getLimit();
        long windowSeconds = request.getPolicy().getWindowSeconds();
        long now = request.getTimestamp();

        long windowMs = windowSeconds * 1000;

        String redisKey = key + ":swl";

        return redisExecutor.execute(

                () -> {

                    List<Long> result = redisTemplate.execute(
                            slidingWindowLogScript,
                            Collections.singletonList(redisKey),
                            String.valueOf(limit),
                            String.valueOf(windowMs),
                            String.valueOf(now)
                    );

                    if (result == null || result.size() < 3) {
                        return fallbackMethod(now);
                    }

                    boolean allowed = result.get(0) == 1;
                    long remaining = result.get(1);
                    long retryAfterMs = result.get(2);

                    long resetTime = now + retryAfterMs;

                    return RLSResponse.builder()
                            .allowed(allowed)
                            .remaining(remaining)
                            .retryAfterMs(retryAfterMs)
                            .resetTime(resetTime)
                            .message(allowed ? "Allowed" : "Rate limit exceeded")
                            .build();
                },

                () -> fallbackMethod(now)
        );
    }
}