package com.p1rls.rls.controller;

import com.p1rls.rls.model.RLSResponse;
import com.p1rls.rls.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ratelimit")
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/checkRateLimit")
    public RLSResponse checkRateLimit(HttpServletRequest servletRequest, @RequestParam String clientId, @RequestParam String api) {
        return rateLimiterService.checkRateLimit(servletRequest, clientId, api);
    }
}
