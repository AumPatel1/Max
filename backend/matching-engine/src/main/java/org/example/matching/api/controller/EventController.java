package org.example.matching.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.matching.api.dto.EventRequest;
import org.example.matching.api.dto.MarketEvent;
import org.example.matching.api.service.MarketManagmentService;
import org.example.matching.api.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final MarketManagmentService managementService;
    private final SettlementService settlementService;

    @PostMapping("/create")
    public ResponseEntity<MarketEvent> create(@RequestBody EventRequest req) {
        MarketEvent event = managementService.createEvent(
                req.getId(),
                req.getQuestions(),
                req.getYesTicker(),
                req.getNoTicker(),
                req.getExpiry()
                );
        return ResponseEntity.ok(event);

    }

    @PostMapping("/{id}/settle/{outcome}")
    public ResponseEntity<String> settle(@PathVariable String id, @PathVariable String outcome) {
        settlementService.settleEvent(id, outcome);
        return ResponseEntity.ok("Event " + id + " settled with outcome: " + outcome);

    }
}