package com.p1rls.rls.service;

import com.p1rls.rls.config.RLConfig;
import com.p1rls.rls.model.StrategyPolicy;
import org.springframework.stereotype.Service;

// Mess with .yaml
@Service
public class PolicyService {

    private final RLConfig rlConfig;

    public PolicyService(RLConfig rlConfig) {
        this.rlConfig = rlConfig;
    }

    public StrategyPolicy getPolicy(String api) {
        return rlConfig.getPolicies().stream()
                .filter(p -> p.getApi().equals(api))
                .findFirst()
                .map(p -> {
                    StrategyPolicy policy = new StrategyPolicy();
                    policy.setLimit(p.getLimit());
                    policy.setWindowSeconds(p.getWindowSeconds());
                    policy.setAlgorithm(p.getAlgorithm());
                    return policy;
                })
                .orElseThrow(() -> new IllegalArgumentException("No policy configured for API: " + api));
    }
}
