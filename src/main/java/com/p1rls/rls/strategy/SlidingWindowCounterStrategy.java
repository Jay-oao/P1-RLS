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

import java.util.Arrays;
import java.util.List;

import static com.p1rls.rls.utils.RedisFailureMethod.fallbackMethod;

@Component
@SuppressWarnings("unchecked")
public class SlidingWindowCounterStrategy implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;

    private final RedisScriptLoader scriptLoader;

    private final RedisExecutorUtil redisExecutor;

    private DefaultRedisScript<List> slidingWindowCounterScript;

    public SlidingWindowCounterStrategy(StringRedisTemplate redisTemplate, RedisScriptLoader scriptLoader, RedisExecutorUtil redisExecutor) {
        this.redisTemplate = redisTemplate;
        this.scriptLoader = scriptLoader;
        this.redisExecutor = redisExecutor;
    }

    @PostConstruct
    public void init() {
        slidingWindowCounterScript = scriptLoader.load("scripts/sliding_window_counter.lua");
    }

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.SLIDING_WINDOW_COUNTER;
    }

    @Override
    public RLSResponse allowRequest(RLSRequest request) {

        String key = request.getKey();
        long limit = request.getPolicy().getLimit();
        long windowSeconds = request.getPolicy().getWindowSeconds();
        long now = request.getTimestamp();

        long windowMs = windowSeconds * 1000;

        long windowStart = (now / windowMs) * windowMs;
        long prevWindowStart = windowStart - windowMs;

        String currKey = key + ":swc:" + windowStart;
        String prevKey = key + ":swc:" + prevWindowStart;

        return redisExecutor.execute(

                () -> {

                    List<Long> result = redisTemplate.execute(
                            slidingWindowCounterScript,
                            Arrays.asList(currKey, prevKey),
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

                    return RLSResponse.builder()
                            .allowed(allowed)
                            .remaining(remaining)
                            .retryAfterMs(retryAfterMs)
                            .resetTime(now + retryAfterMs)
                            .message(allowed ? "Allowed" : "Rate limit exceeded")
                            .build();
                },

                () -> fallbackMethod(now)
        );
    }
}