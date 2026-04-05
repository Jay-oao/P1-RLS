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
public class TokenBucketStrategy implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;

    private final RedisExecutorUtil redisExecutorUtil;

    private final RedisScriptLoader scriptLoader;

    private DefaultRedisScript<List> tokenBucketScript;

    public TokenBucketStrategy(StringRedisTemplate redisTemplate, RedisExecutorUtil redisExecutorUtil, RedisScriptLoader scriptLoader) {
        this.redisTemplate = redisTemplate;
        this.redisExecutorUtil = redisExecutorUtil;
        this.scriptLoader = scriptLoader;
    }

    @PostConstruct
    public void init() {
        tokenBucketScript = scriptLoader.load("scripts/token_bucket.lua");
    }

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.TOKEN_BUCKET;
    }

    @Override
    public RLSResponse allowRequest(RLSRequest request) {

        String key = request.getKey();
        long capacity = request.getPolicy().getLimit();
        long refillRate = request.getPolicy().getWindowSeconds();
        long now = request.getTimestamp();

        String tokensKey = key + ":tokens";
        String timestampKey = key + ":ts";

        return redisExecutorUtil.execute(

                () -> {

                    List<Long> result = redisTemplate.execute(
                            tokenBucketScript,
                            Arrays.asList(tokensKey, timestampKey),
                            String.valueOf(capacity),
                            String.valueOf(refillRate),
                            String.valueOf(now)
                    );

                    if (result == null || result.size() < 4) {
                        throw new RuntimeException("Invalid Lua response");
                    }

                    boolean allowed = result.get(0) == 1;
                    long remaining = result.get(1);
                    long retryAfterMs = result.get(2);
                    long timeToFullMs = result.get(3);

                    long resetTime = now + timeToFullMs;

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