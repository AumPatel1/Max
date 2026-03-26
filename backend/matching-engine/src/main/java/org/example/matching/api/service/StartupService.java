package org.example.matching.api.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.matching.entity.MarketEventEntity;
import org.example.matching.entity.OrderEntity;
import org.example.matching.matching.MatchingEngine;
import org.example.matching.model.Order;
import org.example.matching.model.OrderSide;
import org.example.matching.repository.MarketEventRepository;
import org.example.matching.repository.OrderJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartupService {

    private final MatchingEngine matchingEngine;
    private final OrderJpaRepository orderJpaRepo;
    private final MarketEventRepository eventRepo;
    private final EventTickerRegistry eventTickerRegistry;

    @PostConstruct
    public void rebuildFromDatabase() {
        rebuildEventRegistry();
        rebuildOrderBooks();
    }

    /** Re-registers all non-settled event ticker pairs so synthetic shorts still work. */
    private void rebuildEventRegistry() {
        List<MarketEventEntity> activeEvents = eventRepo.findByStatusNot("SETTLED");
        for (MarketEventEntity e : activeEvents) {
            eventTickerRegistry.registerPair(e.getYesTicker(), e.getNoTicker());
            log.info("Restored event ticker pair: {} / {}", e.getYesTicker(), e.getNoTicker());
        }
        log.info("Restored {} event(s) from database", activeEvents.size());
    }

    /** Reloads all OPEN/PARTIALLY_FILLED orders back into the in-memory order books. */
    private void rebuildOrderBooks() {
        List<OrderEntity> activeOrders = orderJpaRepo.findAllActive();
        for (OrderEntity e : activeOrders) {
            Order order = new Order(
                    e.getId(),
                    e.getUserId(),
                    e.getPrice(),
                    e.getRemainingQty(),   // use remaining qty, not original
                    e.getTimestamp(),
                    OrderSide.valueOf(e.getSide()),
                    e.getInstrument()
            );
            matchingEngine.addRestingOrder(order);
        }
        log.info("Restored {} active order(s) into matching engine from database", activeOrders.size());
    }
}
