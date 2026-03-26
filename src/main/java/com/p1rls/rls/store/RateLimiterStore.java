package com.p1rls.rls.store;

//Change to redis later
public interface RateLimiterStore {
    int get(String key);

    void put(String key, int value);

    int increment(String key, int delta);

    long getTimestamp(String key);

    void setTimestamp(String key, long timestamp);

    void delete(String key);
}
