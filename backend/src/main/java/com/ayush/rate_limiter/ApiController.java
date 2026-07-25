package com.ayush.rate_limiter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/resource")
    public ResponseEntity<String> getResource() {
        return ResponseEntity.ok("✅ SUCCESS: You have accessed the protected resource!");
    }
}