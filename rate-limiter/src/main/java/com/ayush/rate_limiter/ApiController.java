package com.ayush.rate_limiter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {
    @GetMapping("/api/ping")
    public String ping(){
        return "PONG! API is working.";
    }
}
