package com.p1rls.rls.utils;

import com.p1rls.rls.model.RLSResponse;

public class RedisFailureMethod {

    public static RLSResponse fallbackMethod(long now) {
        return RLSResponse.builder()
                .allowed(true)
                .remaining(-1)
                .retryAfterMs(0)
                .resetTime(now)
                .message("Allowed (Redis failure fallback)")
                .build();
    }
}
