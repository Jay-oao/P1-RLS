package com.p1rls.rls.strategy;

import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;

public class SlidingWindowLogStrategy implements RateLimiterStrategy {

    @Override
    public RLSResponse allowRequest(RLSRequest request) {
        return null;
    }
}
