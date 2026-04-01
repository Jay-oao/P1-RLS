package com.p1rls.rls.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class RLSResponse {
    private boolean allowed;

    private int remaining;

    private long retryAfterMs;

    private long resetTime;

    private String message;
}
