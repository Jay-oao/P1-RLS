package com.p1rls.rls.strategy;

import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;

public interface RateLimiterStrategy {
    RLSResponse allowRequest(RLSRequest request);
}
