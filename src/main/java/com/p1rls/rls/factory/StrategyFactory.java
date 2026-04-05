package com.p1rls.rls.factory;

import com.p1rls.rls.model.Algorithm;
import com.p1rls.rls.strategy.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategyFactory {

    private final Map<Algorithm, RateLimiterStrategy> strategyMap;

    public StrategyFactory(List<RateLimiterStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        RateLimiterStrategy::getAlgorithm,
                        Function.identity()
                ));
    }

    public RateLimiterStrategy getStrategy(Algorithm algorithm) {
        RateLimiterStrategy strategy = strategyMap.get(algorithm);

        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }

        return strategy;
    }
}