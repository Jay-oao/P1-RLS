package com.p1rls.rls.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RLSRequest {
    private String key;

    private StrategyPolicy policy;

    private long timestamp;

    //    NOT IN USE
    //    private int cost = 1;

    //    private String requestId;
}
