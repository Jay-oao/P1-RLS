package com.p1rls.rls.service;

import com.p1rls.rls.factory.StrategyFactory;
import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;
import com.p1rls.rls.model.StrategyPolicy;
import com.p1rls.rls.strategy.RateLimiterStrategy;
import com.p1rls.rls.utils.ExtractIP;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class BasicRateLimiterServiceImpl implements RateLimiterService {

    private final PolicyService policyService;

    private final StrategyFactory strategyFactory;

    public BasicRateLimiterServiceImpl(PolicyService policyService, StrategyFactory strategyFactory) {
        this.policyService = policyService;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public RLSResponse checkRateLimit(HttpServletRequest httpServletRequest, String clientId, String api) {
        String ip = ExtractIP.extractIp(httpServletRequest);
        String key = clientId + ":" + api + ":" + ip;
        StrategyPolicy policy = policyService.getPolicy(api);
        RLSRequest rlsRequest = RLSRequest.builder()
                .key(key)
                .policy(policy)
                .timestamp(System.currentTimeMillis()).
                build();
        RateLimiterStrategy strategy = strategyFactory.getStrategy(policy.getAlgorithm());
        return strategy.allowRequest(rlsRequest);
    }
}
