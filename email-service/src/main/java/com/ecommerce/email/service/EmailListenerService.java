package com.ecommerce.email.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailListenerService {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "email-service-group")
    public void listenOrderEvents(String message) {
        try {
            log.info("Received order event: {}", message);

            // Parse the JSON message
            JsonNode jsonNode = objectMapper.readTree(message);
            Long orderId = jsonNode.get("orderId").asLong();
            Long productId = jsonNode.get("productId").asLong();
            Integer quantity = jsonNode.get("quantity").asInt();
            String status = jsonNode.get("status").asText();

            // Simulate sending an email
            System.out.println("=== EMAIL NOTIFICATION ===");
            System.out.println("Order ID: " + orderId);
            System.out.println("Product ID: " + productId);
            System.out.println("Quantity: " + quantity);
            System.out.println("Status: " + status);
            System.out.println("Email sent successfully!");
            System.out.println("==========================");

        } catch (JsonProcessingException e) {
            log.error("Error processing order event: {}", e.getMessage());
            System.out.println("Lỗi khi xử lý sự kiện đơn hàng: " + e.getMessage());
        }
    }
}