package com.ayush.rate_limiter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        // Load the Lua script from src/main/resources/token_bucket.lua
        this.script = new DefaultRedisScript<>();
        this.script.setLocation(new ClassPathResource("token_bucket.lua"));
        this.script.setResultType(Long.class);
    }

    public boolean isAllowed(String apiKey) {
        long maxTokens;
        long refillRate;

        // Simulate a database check for API key tiers
        if ("pro_token_999".equals(apiKey)) {
            // Pro Tier: 50 requests capacity, refills 10 tokens per second
            maxTokens = 50;
            refillRate = 10;
        } else {
            // Free Tier (Default): 5 requests capacity, refills 1 token per second
            maxTokens = 5;
            refillRate = 1;
        }

        long now = Instant.now().getEpochSecond();
        long requestedTokens = 1;

        // Redis Key format: "rate_limit:pro_token_999"
        String key = "rate_limit:" + apiKey;

        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(maxTokens),
                String.valueOf(refillRate),
                String.valueOf(now),
                String.valueOf(requestedTokens)
        );

        return Long.valueOf(1).equals(result);
    }
}