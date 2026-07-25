package com.ayush.rate_limiter;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaAuditConsumer {

    // This annotation tells Spring to constantly listen to our topic
    @KafkaListener(topics = "rate-limit-alerts", groupId = "security-audit-group")
    public void consumeBlockAlert(String message) {

        // In a production environment, you might save this to a database,
        // trigger an email via MailSense AI, or send a Slack/Discord webhook.
        // For now, we will print a distinct log to prove we caught it!

        System.out.println("🛡️ AUDIT ALERT RECEIVED FROM KAFKA: " + message);
    }
}