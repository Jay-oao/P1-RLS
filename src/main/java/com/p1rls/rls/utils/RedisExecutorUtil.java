package com.p1rls.rls.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;


// Can add more fallback logic for redis here
@Component
public class RedisExecutorUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisExecutorUtil.class);

    /**
     * Executes a Redis operation safely.
     * If Redis fails, fallback logic is executed.
     */
    public <T> T execute(Supplier<T> operation, Supplier<T> fallback) {
        try {
            return operation.get();
        } catch (Exception e) {
            logger.error("Redis operation failed. Executing fallback.", e);
            return fallback.get();
        }
    }
}
