package com.p1rls.rls.model;

import lombok.Data;

@Data
public class StrategyPolicy {
    private String api;

    private int limit;

    private int windowSeconds;

    public Algorithm algorithm;
}
