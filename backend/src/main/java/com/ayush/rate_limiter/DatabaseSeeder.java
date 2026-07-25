package com.ayush.rate_limiter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final ApiKeyRepository apiKeyRepository;

    public DatabaseSeeder(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only seed the database if it is currently empty
        if (apiKeyRepository.count() == 0) {
            System.out.println("Seeding database with test API keys...");
            apiKeyRepository.save(new ApiKey("free_token_123", "FREE", 5, 1));
            apiKeyRepository.save(new ApiKey("pro_token_999", "PRO", 50, 10));
            System.out.println("Test API keys seeded successfully!");
        } else {
            System.out.println("Database already contains API keys. Skipping seed.");
        }
    }
}