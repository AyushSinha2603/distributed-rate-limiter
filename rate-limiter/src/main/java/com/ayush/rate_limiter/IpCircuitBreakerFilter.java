package com.ayush.rate_limiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class IpCircuitBreakerFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    // Threshold: Allow max 20 bad requests per minute per IP before blacklisting
    private static final int MAX_BAD_REQUESTS = 20;
    private static final long BLOCK_DURATION_MINUTES = 5;

    public IpCircuitBreakerFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String blacklistKey = "ip_blacklist:" + clientIp;
        String trackerKey = "ip_tracker:" + clientIp;

        // 1. Check if the IP is currently blacklisted in Redis
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"IP BLOCKED: Too many suspicious requests. Try again later.\"}");
            return;
        }

        // Proceed with the request down the filter chain
        filterChain.doFilter(request, response);

        // 2. Post-processing: If the response resulted in an error/rate-limit status, track it
        int status = response.getStatus();
        if (status == 401 || status == 403 || status == 429) {
            Long strikes = redisTemplate.opsForValue().increment(trackerKey);
            if (strikes != null && strikes == 1) {
                // Initialize a 1-minute expiration window for the strike counter
                redisTemplate.expire(trackerKey, Duration.ofMinutes(1));
            }

            if (strikes != null && strikes >= MAX_BAD_REQUESTS) {
                // Blacklist the IP for 5 minutes and clear the strike tracker
                redisTemplate.opsForValue().set(blacklistKey, "BLACKLISTED", Duration.ofMinutes(BLOCK_DURATION_MINUTES));
                redisTemplate.delete(trackerKey);
                System.out.println("🚨 CIRCUIT BREAKER TRIPPED: Blacklisted malicious IP -> " + clientIp);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}