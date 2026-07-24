package com.ayush.rate_limiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    // Constructor Injection
    public RateLimitInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. Extract the API key from the HTTP headers
        String apiKey = request.getHeader("X-API-KEY");

        // 2. Reject requests that don't have the header
        if (apiKey == null || apiKey.trim().isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("HTTP 401: Unauthorized. Missing X-API-KEY header!");
            return false;
        }

        // 3. Pass the dynamic API key to our service
        boolean isAllowed = rateLimiterService.isAllowed(apiKey);

        if (!isAllowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("HTTP 429: Too Many Requests. Bucket is empty for key: " + apiKey);
            return false;
        }

        return true;
    }
}