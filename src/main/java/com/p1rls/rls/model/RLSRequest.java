package com.p1rls.rls.model;

import lombok.Data;

@Data
public class RLSRequest {
    private String key;

    private int limit;

    private int windowSeconds;

    public Algorithm algorithm;

    //    NOT IN USE
    //    private int cost = 1;
    //    private long timestamp;
    //    private String requestId;
}
