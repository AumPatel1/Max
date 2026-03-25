package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.matching.Repository.MarketRepository;
import org.example.matching.Repository.OrderJpaRepository;
import org.example.matching.Repository.TradeRepository;
import org.example.matching.entity.Order;
import org.example.matching.entity.Market;
import org.example.matching.entity.Trade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradePersistenceService {

    private final TradeRepository tradeRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final MarketRepository marketRepository;

    @Transactional
    public void saveTrade(org.example.matching.model.Trade modelTrade) {
        try {
            // Create a default market for now
            Market market = marketRepository.findById(1L)
                    .orElseGet(() -> {
                        Market newMarket = new Market();
                        newMarket.setQuestion("BTC-USD");
                        newMarket.setStatus(Market.MarketStatus.OPEN);
                        return marketRepository.save(newMarket);
                    });

            // Create database trade
            Trade dbTrade = new Trade();
            dbTrade.setMarket(market);
            dbTrade.setPrice(BigDecimal.valueOf(modelTrade.getPrice()));
            dbTrade.setQuantity((int) modelTrade.getQuantity());
            dbTrade.setTotalValue(BigDecimal.valueOf(modelTrade.getPrice() * modelTrade.getQuantity()));

            // Set orders (simplified for now)
            Order buyOrder = new Order();
            try {
                buyOrder.setId(Long.valueOf(modelTrade.getBuyOrderId()));
            } catch (Exception e) {
                buyOrder.setId(0L); // Default ID
            }
            dbTrade.setBuyOrder(buyOrder);

            Order sellOrder = new Order();
            try {
                sellOrder.setId(Long.valueOf(modelTrade.getSellOrderId()));
            } catch (Exception e) {
                sellOrder.setId(0L); // Default ID
            }
            dbTrade.setSellOrder(sellOrder);

            Trade savedTrade = tradeRepository.save(dbTrade);
            log.info("Saved trade to database: {}", savedTrade.getId());

        } catch (Exception e) {
            log.error("Failed to save trade to database", e);
        }
    }
}
