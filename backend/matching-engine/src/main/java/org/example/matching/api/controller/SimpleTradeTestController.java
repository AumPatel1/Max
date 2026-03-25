package org.example.matching.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.matching.Repository.TradeRepository;
import org.example.matching.entity.Trade;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/test/trade")
@RequiredArgsConstructor
public class SimpleTradeTestController {

    private final TradeRepository tradeRepository;

    @PostMapping("/simple")
    public ResponseEntity<String> createSimpleTrade() {
        try {
            Trade trade = new Trade();
            trade.setPrice(BigDecimal.valueOf(1000.50));
            trade.setQuantity(10);
            trade.setTotalValue(BigDecimal.valueOf(10005.00));
            
            Trade saved = tradeRepository.save(trade);
            return ResponseEntity.ok("Trade saved with ID: " + saved.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTradeCount() {
        return ResponseEntity.ok(tradeRepository.count());
    }
}
