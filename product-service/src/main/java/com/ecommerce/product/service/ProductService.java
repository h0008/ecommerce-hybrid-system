package com.ecommerce.product.service;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.listener.OnMessageListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    // Assignment 1: Redis Cache/TTL for product information
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        log.info("Fetching product with id: {} from database", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    // Assignment 1 & 4: Cache eviction when product information is updated
    @Caching(
            evict = {
                    @CacheEvict(value = "products", key = "#id"),
                    @CacheEvict(value = "products", allEntries = true) // Also clear all products cache if needed
            }
    )
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Update fields
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());

        Product savedProduct = productRepository.save(existingProduct);
        log.info("Updated product with id: {}", id);
        return savedProduct;
    }

    // Assignment 4: Redis Pub/Sub Subscriber to clear cache
    public void setupCacheClearingSubscriber() {
        RTopic topic = redissonClient.getTopic("promotion-updates");
        topic.addListener(Map.class, new OnMessageListener<Map<String, Object>>() {
            @Override
            public void onMessage(CharSequence channel, Map<String, Object> message) {
                try {
                    Object productIdObj = message.get("product_id");
                    if (productIdObj != null) {
                        Long productId = Long.parseLong(productIdObj.toString());
                        log.info("Received promotion update for product_id: {}, clearing cache", productId);

                        // Evict specific product from cache
                        redisTemplate.delete("products::" + productId);
                        // Also clear all products cache as promotion might affect listing
                        redisTemplate.delete("products");

                        log.info("Cache cleared for product_id: {}", productId);
                    }
                } catch (Exception e) {
                    log.error("Error processing promotion update message: {}", e.getMessage());
                }
            }
        });
        log.info("Subscribed to promotion-updates channel for cache clearing");
    }
}