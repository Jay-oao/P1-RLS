package com.p1rls.rls.model;

import lombok.Builder;

@Builder
public class RLSResponse {
    private boolean allowed;

    private int remaining;

    private long retryAfterMs;

    private long resetTime;

    private String message;
}
