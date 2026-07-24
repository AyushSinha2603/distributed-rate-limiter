package com.ayush.rate_limiter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/api/ping")
    public ResponseEntity<String> ping() {
        // Look how clean this is! The controller doesn't even know the rate limiter exists.
        return ResponseEntity.ok("Pong! API is working and request is allowed.");
    }
}