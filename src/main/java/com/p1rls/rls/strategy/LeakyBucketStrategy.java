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
public class LeakyBucketStrategy implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;

    private final RedisScriptLoader scriptLoader;

    private final RedisExecutorUtil redisExecutor;

    private DefaultRedisScript<List> leakyBucketScript;

    public LeakyBucketStrategy(StringRedisTemplate redisTemplate, RedisScriptLoader scriptLoader, RedisExecutorUtil redisExecutor) {
        this.redisTemplate = redisTemplate;
        this.scriptLoader = scriptLoader;
        this.redisExecutor = redisExecutor;
    }

    @PostConstruct
    public void init() {
        leakyBucketScript = scriptLoader.load("scripts/leaky_bucket.lua");
    }

    @Override
    public Algorithm getAlgorithm() {
        return Algorithm.LEAKY_BUCKET;
    }

    @Override
    public RLSResponse allowRequest(RLSRequest request) {

        String key = request.getKey();
        long capacity = request.getPolicy().getLimit();
        long drainTimeSeconds = request.getPolicy().getWindowSeconds();
        long now = request.getTimestamp();

        double leakRatePerMs = (double) capacity / (drainTimeSeconds * 1000.0);

        String bucketKey = key + ":lb:level";
        String lastUpdatedTsKey = key + ":lb:ts";

        return redisExecutor.execute(

                () -> {

                    List<Long> result = redisTemplate.execute(
                            leakyBucketScript,
                            Arrays.asList(bucketKey, lastUpdatedTsKey),
                            String.valueOf(capacity),
                            String.valueOf(leakRatePerMs),
                            String.valueOf(now),
                            String.valueOf(drainTimeSeconds)
                    );

                    if (result == null || result.size() < 4) {
                        return fallbackMethod(now);
                    }

                    boolean allowed = result.get(0) == 1;
                    long remaining = result.get(1);
                    long retryAfterMs = result.get(2);
                    long resetTime = result.get(3);

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