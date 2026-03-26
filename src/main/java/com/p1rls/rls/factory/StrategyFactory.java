package com.p1rls.rls.factory;

import com.p1rls.rls.model.Algorithm;
import com.p1rls.rls.strategy.*;

public class StrategyFactory {
    public static RateLimiterStrategy getStrategy(Algorithm algorithm) {
        switch (algorithm) {

            case FIXED_WINDOW:
                return new FixedWindowStrategy();

            case SLIDING_WINDOW_COUNTER:
                return new SlidingWindowCounterStrategy();

            case SLIDING_WINDOW_LOG:
                return new SlidingWindowLogStrategy();

            case TOKEN_BUCKET:
                return new TokenBucketStrategy();

            case LEAKY_BUCKET:
                return new LeakyBucketStrategy();

            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }
}
