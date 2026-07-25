package com.ayush.rate_limiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.CompletableFuture;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;
    private final KafkaAuditProducer kafkaAuditProducer;

    public RateLimitInterceptor(RateLimiterService rateLimiterService, KafkaAuditProducer kafkaAuditProducer) {
        this.rateLimiterService = rateLimiterService;
        this.kafkaAuditProducer = kafkaAuditProducer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("API Key is missing\n");
            return false;
        }

        boolean allowed = rateLimiterService.isAllowed(apiKey);

        if (!allowed) {
            // 🚨 Fire and forget! Run Kafka in a background thread so it doesn't block the HTTP response
            CompletableFuture.runAsync(() -> {
                kafkaAuditProducer.logBlockedRequest(apiKey, request.getRequestURI());
            });

            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("HTTP 429: Too Many Requests. Bucket is empty for key: " + apiKey + "\n");
            return false;
        }

        return true;
    }
}