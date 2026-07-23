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

    public boolean isAllowed(String clientId) {
        // Define rate limiter configurations
        long maxTokens = 5;       // Bucket capacity: 5 tokens max
        long refillRate = 1;      // Refill rate: 1 token per second
        long now = Instant.now().getEpochSecond();
        long requestedTokens = 1;

        // Redis Key format: "rate_limit:user123"
        String key = "rate_limit:" + clientId;

        // Execute atomic Lua script
        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(key),  // KEYS[1]
                String.valueOf(maxTokens),        // ARGV[1]
                String.valueOf(refillRate),       // ARGV[2]
                String.valueOf(now),              // ARGV[3]
                String.valueOf(requestedTokens)   // ARGV[4]
        );

        // Script returns 1 for allowed, 0 for rate-limited
        return result != null && result == 1;
    }
}