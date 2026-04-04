package com.p1rls.rls.strategy;

import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;
import com.p1rls.rls.utils.RedisExecutorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TokenBucketStrategy implements RateLimiterStrategy {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisExecutorUtil redisExecutorUtil;

    @Autowired
    private DefaultRedisScript<List> tokenBucketScript;

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

                () -> RLSResponse.builder()
                        .allowed(true)
                        .remaining(-1)
                        .retryAfterMs(0)
                        .resetTime(now)
                        .message("Allowed (Redis failure fallback)")
                        .build()
        );
    }
}