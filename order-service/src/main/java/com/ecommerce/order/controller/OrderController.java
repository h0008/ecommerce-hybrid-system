package com.ecommerce.order.controller;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // Place a new order (produces Kafka event)
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestParam Long productId, @RequestParam Integer quantity) {
        Order order = orderService.placeOrder(productId, quantity);
        return ResponseEntity.ok(order);
    }

    // Flash sale purchase with distributed lock
    @PostMapping("/flash-sale/{productId}")
    public ResponseEntity<String> flashSalePurchase(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        boolean success = orderService.purchaseWithLock(productId, quantity);
        if (success) {
            return ResponseEntity.ok("Flash sale purchase successful");
        } else {
            return ResponseEntity.badRequest().body("Flash sale purchase failed - could not acquire lock or insufficient stock");
        }
    }
}