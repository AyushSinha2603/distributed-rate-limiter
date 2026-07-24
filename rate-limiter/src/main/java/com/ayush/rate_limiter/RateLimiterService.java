package com.ayush.rate_limiter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;
    private final ApiKeyRepository apiKeyRepository;

    public RateLimiterService(StringRedisTemplate redisTemplate, ApiKeyRepository apiKeyRepository) {
        this.redisTemplate = redisTemplate;
        this.apiKeyRepository = apiKeyRepository;

        this.script = new DefaultRedisScript<>();
        this.script.setLocation(new ClassPathResource("token_bucket.lua"));
        this.script.setResultType(Long.class);
    }

    public boolean isAllowed(String apiKey) {
        // 1. Fetch the API key configuration directly from PostgreSQL
        Optional<ApiKey> keyDataOpt = apiKeyRepository.findById(apiKey);

        if (keyDataOpt.isEmpty()) {
            return false; // Reject if API key does not exist in DB
        }

        ApiKey keyData = keyDataOpt.get();

        long now = Instant.now().getEpochSecond();
        long requestedTokens = 1;
        String redisKey = "rate_limit:" + apiKey;

        // 2. Execute Lua script using dynamic database tier settings
        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(redisKey),
                String.valueOf(keyData.getMaxTokens()),
                String.valueOf(keyData.getRefillRate()),
                String.valueOf(now),
                String.valueOf(requestedTokens)
        );

        return Long.valueOf(1).equals(result);
    }
}