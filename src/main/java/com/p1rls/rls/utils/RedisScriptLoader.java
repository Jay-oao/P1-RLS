package com.p1rls.rls.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisScriptLoader {

    public DefaultRedisScript<List> load(String scriptPath) {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(scriptPath));
        script.setResultType(List.class);
        return script;
    }
}
