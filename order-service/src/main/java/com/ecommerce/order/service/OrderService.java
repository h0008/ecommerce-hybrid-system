package com.ecommerce.order.service;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final String ORDER_EVENTS_TOPIC = "order-events";

    // Assignment 2: Kafka Producer for order creation
    @Transactional
    public Order placeOrder(Long productId, Integer quantity) {
        Order order = new Order();
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setStatus("CREATED");
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {}", savedOrder.getId());

        // Send Kafka event
        Map<String, Object> event = new HashMap<>();
        event.put("orderId", savedOrder.getId());
        event.put("productId", productId);
        event.put("quantity", quantity);
        event.put("status", "CREATED");

        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, message);
            log.info("Sent OrderCreatedEvent to topic {}: {}", ORDER_EVENTS_TOPIC, message);
        } catch (JsonProcessingException e) {
            log.error("Error sending Kafka event: {}", e.getMessage());
        }

        return savedOrder;
    }

    // Assignment 3: Redisson Distributed Lock for Flash Sale
    @Transactional
    public boolean purchaseWithLock(Long productId, Integer quantity) {
        String lockKey = "lock:product:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Try to acquire lock with 3 seconds wait time and 10 seconds lease time
            if (lock.tryLock(3, 10, java.util.concurrent.TimeUnit.SECONDS)) {
                try {
                    // Simulate checking stock and reducing stock
                    // In a real system, we would call a product service or access product repository
                    // For this assignment, we assume stock is available and just log
                    log.info("Flash sale purchase attempt for productId: {}, quantity: {}", productId, quantity);
                    // Simulate successful purchase
                    // Normally, we would check stock and then reduce it
                    // For demonstration, we'll just log and return true
                    log.info("Flash sale purchase successful for productId: {}, quantity: {}", productId, quantity);
                    return true;
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("Could not acquire lock for productId: {} after waiting", productId);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while trying to acquire lock for productId: {}", productId);
            return false;
        }
    }
}