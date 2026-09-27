package com.ecommerce.promotion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Assignment 4: Redis Publisher for price changes
    public void publishPriceChange(Long productId) {
        Map<String, Object> message = new HashMap<>();
        message.put("product_id", productId);

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend("promotion-updates", jsonMessage);
            log.info("Published promotion update to channel promotion-updates: {}", jsonMessage);
        } catch (Exception e) {
            log.error("Error publishing promotion update: {}", e.getMessage());
        }
    }
}