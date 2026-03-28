package com.p1rls.rls.service;

import com.p1rls.rls.model.RLSRequest;
import com.p1rls.rls.model.RLSResponse;
import jakarta.servlet.http.HttpServletRequest;

public class BasicRateLimiterServiceImpl implements RateLimiterService {

    @Override
    public RLSResponse checkRateLimit(HttpServletRequest httpServletRequest, String clientId, String api) {
        return null;
    }
}
