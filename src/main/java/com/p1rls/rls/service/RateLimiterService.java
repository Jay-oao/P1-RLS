package com.p1rls.rls.service;

import com.p1rls.rls.model.RLSResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface RateLimiterService {
    public RLSResponse checkRateLimit(
            HttpServletRequest httpServletRequest,
            String clientId,
            String api
    );
}
