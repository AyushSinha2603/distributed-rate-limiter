package com.ayush.rate_limiter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    private String key;

    private String tier; // e.g., "FREE" or "PRO"
    private int maxTokens;
    private int refillRate;

    // Default constructor required by JPA
    public ApiKey() {}

    public ApiKey(String key, String tier, int maxTokens, int refillRate) {
        this.key = key;
        this.tier = tier;
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

    public int getRefillRate() { return refillRate; }
    public void setRefillRate(int refillRate) { this.refillRate = refillRate; }
}