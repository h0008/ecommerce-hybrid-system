package com.ecommerce.promotion.controller;

import com.ecommerce.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotion")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {

    private final PromotionService promotionService;

    // Endpoint to trigger promotion update (for testing)
    @PostMapping("/publish")
    public ResponseEntity<String> publishPriceChange(@RequestParam Long productId) {
        promotionService.publishPriceChange(productId);
        return ResponseEntity.ok("Promotion update published for productId: " + productId);
    }
}