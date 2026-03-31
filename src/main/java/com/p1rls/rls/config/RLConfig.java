package com.p1rls.rls.config;

import com.p1rls.rls.model.StrategyPolicy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@Data
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RLConfig {
    private List<StrategyPolicy> policies;
}
