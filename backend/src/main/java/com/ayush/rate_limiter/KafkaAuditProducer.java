package com.ayush.rate_limiter;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaAuditProducer {

    // Spring Boot auto-configures a <String, String> template by default
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "rate-limit-alerts";

    public KafkaAuditProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void logBlockedRequest(String apiKey, String path) {
        // Create a simple JSON string to log the event details
        String payload = String.format(
                "{\"apiKey\":\"%s\", \"path\":\"%s\", \"timestamp\":%d}",
                apiKey, path, System.currentTimeMillis()
        );

        // Send the message to the Kafka topic
        kafkaTemplate.send(TOPIC, apiKey, payload);

        System.out.println("🚨 KAFKA EVENT PUBLISHED: " + payload);
    }
}