package com.p1rls.rls.strategy;

import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

// Client level
public class FixedWindowStrategy implements RateLimiterStrategy {

    private final ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public RLSResponse allowRequest(RLSRequest request) {

        String key = request.getKey();
        int limit = request.getPolicy().getLimit();
        int windowSeconds = request.getPolicy().getWindowSeconds();
        long now = request.getTimestamp();

        //1000 because 'now' is in ms
        long windowSize = windowSeconds*1000L;
        long windowStart = (now/windowSize)*windowSize;
        long windowEnd = windowStart + windowSize;

        String compositeKey = key+"-"+windowStart;

        ReentrantLock lock = locks.computeIfAbsent(compositeKey, k -> new ReentrantLock());
        lock.lock();

        try{
            int current = counter.getOrDefault(compositeKey, 0);

            if(current + 1 <= limit) {
                int updated = counter.merge(compositeKey, 1, Integer::sum);

                return RLSResponse.builder()
                        .allowed(true)
                        .remaining(limit - updated)
                        .retryAfterMs(0)
                        .resetTime(windowEnd)
                        .message("Allowed")
                        .build();
            } else {
                long retryAfter = windowEnd - now;
                return RLSResponse.builder()
                        .allowed(false)
                        .remaining(0)
                        .retryAfterMs(retryAfter)
                        .resetTime(windowEnd)
                        .message("Blocked")
                        .build();

            }
        } finally {
            lock.unlock();
        }
    }
}
