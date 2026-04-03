package com.p1rls.rls.service;

import com.p1rls.rls.factory.StrategyFactory;
import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;
import com.p1rls.rls.model.StrategyPolicy;
import com.p1rls.rls.strategy.RateLimiterStrategy;
import com.p1rls.rls.utils.ExtractIP;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BasicRateLimiterServiceImpl implements RateLimiterService {

    private final PolicyService policyService;

    private final StrategyFactory strategyFactory;

    private static final Logger logger = LoggerFactory.getLogger(BasicRateLimiterServiceImpl.class);

    public BasicRateLimiterServiceImpl(PolicyService policyService, StrategyFactory strategyFactory) {
        this.policyService = policyService;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public RLSResponse checkRateLimit(HttpServletRequest httpServletRequest, String clientId, String api) {

        logger.info("Received rate limit request: clientId={}, api={}", clientId, api);

        String ip = ExtractIP.extractIp(httpServletRequest);
        logger.info("Extracted IP: clientId={}, api={}, ip={}", clientId, api, ip);

        String key = clientId + ":" + api + ":" + ip;
        logger.info("Generated key: {}", key);

        StrategyPolicy policy = policyService.getPolicy(api);
        logger.info("Loaded policy: api={}, limit={}, windowSeconds={}, algorithm={}",
                policy.getApi(),
                policy.getLimit(),
                policy.getWindowSeconds(),
                policy.getAlgorithm());

        RLSRequest rlsRequest = RLSRequest.builder()
                .key(key)
                .policy(policy)
                .timestamp(System.currentTimeMillis())
                .build();
        logger.info("Constructed RLSRequest: key={}, timestamp={}", key, rlsRequest.getTimestamp());

        RateLimiterStrategy strategy = strategyFactory.getStrategy(policy.getAlgorithm());
        logger.info("Resolved strategy: algorithm={}", policy.getAlgorithm());

        RLSResponse response = strategy.allowRequest(rlsRequest);

        logger.info("Rate limit result: key={}, allowed={}, remaining={}",
                key,
                response.isAllowed(),
                response.getRemaining());

        return response;
    }
}