package com.p1rls.rls.factory;

import com.p1rls.rls.model.Algorithm;
import com.p1rls.rls.strategy.FixedWindowStrategy;
import com.p1rls.rls.strategy.RateLimiterStrategy;
import org.springframework.stereotype.Component;

@Component
public class StrategyFactory {

    private final FixedWindowStrategy fixedWindowStrategy;

    public StrategyFactory(FixedWindowStrategy fixedWindowStrategy) {
        this.fixedWindowStrategy = fixedWindowStrategy;
    }

    public RateLimiterStrategy getStrategy(Algorithm algorithm) {
        switch (algorithm) {
            case FIXED_WINDOW:
                return fixedWindowStrategy;
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }
}