package com.ayush.rate_limiter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    private final RateLimiterService rateLimiterService;

    // Constructor Injection for your new service
    public ApiController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/api/ping")
    public ResponseEntity<String> ping() {
        // Hardcoding a mock client ID for now (later, this would be an API Key or IP address)
        String clientId = "user_123";

        boolean isAllowed = rateLimiterService.isAllowed(clientId);

        if (isAllowed) {
            return ResponseEntity.ok("Pong! API is working and request is allowed.");
        } else {
            // Return HTTP 429 Too Many Requests if the bucket is empty
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("HTTP 429: Too Many Requests. Bucket is empty!");
        }
    }
}