package com.ayush.rate_limiter;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api-keys")
public class AdminController {

    private final ApiKeyRepository apiKeyRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public AdminController(ApiKeyRepository apiKeyRepository, RedisTemplate<String, String> redisTemplate) {
        this.apiKeyRepository = apiKeyRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiKey> generateKey(@RequestParam String tier) {
        ApiKey newKey = new ApiKey();

        // Generate a secure, random 32-character key
        newKey.setKey(UUID.randomUUID().toString().replace("-", ""));
        newKey.setTier(tier.toUpperCase());

        // Assign capacities based on tier
        if ("PREMIUM".equalsIgnoreCase(tier)) {
            newKey.setMaxTokens(100);
            newKey.setRefillRate(100);
        } else {
            // Default to Free tier limits
            newKey.setMaxTokens(5);
            newKey.setRefillRate(5);
        }

        apiKeyRepository.save(newKey);
        System.out.println("✅ New API Key Generated: " + newKey.getKey());

        return ResponseEntity.ok(newKey);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<String> revokeKey(@PathVariable String key) {
        if (!apiKeyRepository.existsById(key)) {
            return ResponseEntity.notFound().build();
        }

        // 1. Delete from the primary PostgreSQL database
        apiKeyRepository.deleteById(key);

        // 2. Instantly evict the active bucket from Redis to drop active attackers.
        // NOTE: Ensure "rate_limit:" perfectly matches the prefix your RateLimiterService uses!
        redisTemplate.delete("rate_limit:" + key);

        System.out.println("🚫 API Key Revoked: " + key);
        return ResponseEntity.ok("Key " + key + " has been permanently revoked and dropped from cache.");
    }
}