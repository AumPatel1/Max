package org.example.matching.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.matching.api.dto.OrderBookResponse;
import org.example.matching.api.service.MarketDataService;
import org.example.matching.matching.MatchingEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/market")
@RestController
@RequiredArgsConstructor
public class MarketController {
    private final MatchingEngine matchingEngine;
    private final MarketDataService marketDataService;

//    public MarketController(MatchingEngine matchingEngine) {
//        this.matchingEngine = matchingEngine;
//    }

    @GetMapping("/orderbook/{instrument}")
    public ResponseEntity<OrderBookResponse> getOrderBook(@PathVariable String instrument) {
        OrderBookResponse snapshot = matchingEngine.getSnapshot(instrument.toUpperCase());
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping("/ticker/{instrument}")
    public ResponseEntity<Map<String,Object>> getTicker(@PathVariable String instrument){
        return ResponseEntity.ok(marketDataService.getSnapshots(instrument.toUpperCase()));
    }
}
