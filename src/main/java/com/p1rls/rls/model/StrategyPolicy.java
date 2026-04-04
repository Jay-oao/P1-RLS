package com.p1rls.rls.model;

import lombok.Data;

@Data
public class StrategyPolicy {
    private String api;

    private long limit;

    private long windowSeconds;

    public Algorithm algorithm;
}
