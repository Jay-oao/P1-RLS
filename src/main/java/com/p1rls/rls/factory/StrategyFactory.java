package com.p1rls.rls.factory;

import com.p1rls.rls.model.Algorithm;
import com.p1rls.rls.strategy.FixedWindowStrategy;
import com.p1rls.rls.strategy.LeakyBucketStrategy;
import com.p1rls.rls.strategy.RateLimiterStrategy;
import com.p1rls.rls.strategy.TokenBucketStrategy;
import org.springframework.stereotype.Component;

@Component
public class StrategyFactory {

    private final FixedWindowStrategy fixedWindowStrategy;

    private final TokenBucketStrategy tokenBucketStrategy;

    private final LeakyBucketStrategy leakyBucketStrategy;

    public StrategyFactory(FixedWindowStrategy fixedWindowStrategy, TokenBucketStrategy tokenBucketStrategy, LeakyBucketStrategy leakyBucketStrategy) {
        this.fixedWindowStrategy = fixedWindowStrategy;
        this.tokenBucketStrategy = tokenBucketStrategy;
        this.leakyBucketStrategy = leakyBucketStrategy;
    }

    public RateLimiterStrategy getStrategy(Algorithm algorithm) {
        switch (algorithm) {
            case FIXED_WINDOW:
                return fixedWindowStrategy;
            case TOKEN_BUCKET:
                return tokenBucketStrategy;
            case LEAKY_BUCKET:
                return leakyBucketStrategy;
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }
}