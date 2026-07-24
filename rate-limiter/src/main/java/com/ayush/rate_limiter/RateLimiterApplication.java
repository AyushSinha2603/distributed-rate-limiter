package com.ayush.rate_limiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class RateLimiterApplication {

	public static void main(String[] args) {
		// 1. Force the JVM timezone BEFORE Spring Boot initializes anything
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

		// 2. Now start the application
		SpringApplication.run(RateLimiterApplication.class, args);
	}
}