package com.p1rls.rls.factory;

import com.p1rls.rls.model.Algorithm;
import com.p1rls.rls.strategy.FixedWindowStrategy;
import com.p1rls.rls.strategy.RateLimiterStrategy;
import com.p1rls.rls.strategy.TokenBucketStrategy;
import org.springframework.stereotype.Component;

@Component
public class StrategyFactory {

    private final FixedWindowStrategy fixedWindowStrategy;

    private final TokenBucketStrategy tokenBucketStrategy;

    public StrategyFactory(FixedWindowStrategy fixedWindowStrategy, TokenBucketStrategy tokenBucketStrategy) {
        this.fixedWindowStrategy = fixedWindowStrategy;
        this.tokenBucketStrategy = tokenBucketStrategy;
    }

    public RateLimiterStrategy getStrategy(Algorithm algorithm) {
        switch (algorithm) {
            case FIXED_WINDOW:
                return fixedWindowStrategy;
            case TOKEN_BUCKET:
                return tokenBucketStrategy;
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }
}