package com.ayush.rate_limiter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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

    // Micrometer Metrics
    private final Counter allowedRequestsCounter;
    private final Counter blockedRequestsCounter;
    private final Timer rateLimitCheckTimer;

    public RateLimiterService(StringRedisTemplate redisTemplate,
                              ApiKeyRepository apiKeyRepository,
                              MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.apiKeyRepository = apiKeyRepository;

        this.script = new DefaultRedisScript<>();
        this.script.setLocation(new ClassPathResource("token_bucket.lua"));
        this.script.setResultType(Long.class);

        // 1. Register custom metrics
        this.allowedRequestsCounter = meterRegistry.counter("rate_limiter_requests_total", "result", "allowed");
        this.blockedRequestsCounter = meterRegistry.counter("rate_limiter_requests_total", "result", "blocked");
        this.rateLimitCheckTimer = meterRegistry.timer("rate_limiter_check_latency_seconds");
    }

    public boolean isAllowed(String apiKey) {
        // 2. Wrap the entire execution in the timer to track millisecond latency
        return rateLimitCheckTimer.record(() -> {

            Optional<ApiKey> keyDataOpt = apiKeyRepository.findById(apiKey);

            if (keyDataOpt.isEmpty()) {
                blockedRequestsCounter.increment(); // Track rejected bad keys
                return false;
            }

            ApiKey keyData = keyDataOpt.get();

            long now = Instant.now().getEpochSecond();
            long requestedTokens = 1;
            String redisKey = "rate_limit:" + apiKey;

            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(redisKey),
                    String.valueOf(keyData.getMaxTokens()),
                    String.valueOf(keyData.getRefillRate()),
                    String.valueOf(now),
                    String.valueOf(requestedTokens)
            );

            // 3. Track allowed vs blocked requests based on Lua script result
            if (Long.valueOf(1).equals(result)) {
                allowedRequestsCounter.increment();
                return true;
            } else {
                blockedRequestsCounter.increment();
                return false;
            }
        });
    }
}