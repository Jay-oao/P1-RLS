package com.p1rls.rls.model;

import lombok.Data;

@Data
public class RLSRequest {
    private String key;

    private int limit;

    private int windowSeconds;

    public Algorithm algorithm;

    private long timestamp;

    //    NOT IN USE
    //    private int cost = 1;

    //    private String requestId;
}
