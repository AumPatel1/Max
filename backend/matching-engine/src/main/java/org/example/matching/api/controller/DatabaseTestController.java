package org.example.matching.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.matching.Repository.EventRepository;
import org.example.matching.Repository.TradeRepository;
import org.example.matching.Repository.UserRepository;

import org.example.matching.api.service.TradePersistenceService;
import org.example.matching.model.Trade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/db/test")
@RequiredArgsConstructor
public class DatabaseTestController {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TradeRepository tradeRepository;
    private final TradePersistenceService tradePersistenceService;

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getDatabaseCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("users", userRepository.count());
        counts.put("events", eventRepository.count());
        counts.put("trades", tradeRepository.count());
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/trades")
    public ResponseEntity<?> getAllTrades() {
        return ResponseEntity.ok(tradeRepository.findAll());
    }

    @PostMapping("/createtrade")
    public ResponseEntity<String> createTestTrade() {
        // Create a test trade
        Trade testTrade = new Trade();
        testTrade.setBuyOrderId("test-buy-123");
        testTrade.setSellOrderId("test-sell-456");
        testTrade.setPrice(1000L);
        testTrade.setQuantity(5L);
        testTrade.setTimestamp(System.currentTimeMillis());

        // Save to database
        tradePersistenceService.saveTrade(testTrade);

        return ResponseEntity.ok("Test trade created and saved to database");
    }
}
