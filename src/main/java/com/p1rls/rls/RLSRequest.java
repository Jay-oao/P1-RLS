package com.p1rls.rls;

import lombok.Data;

@Data
public class RLSRequest {
    private String key;
    private int limit;
    private int windowSeconds;
    private String algorithm;
}
