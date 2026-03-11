package org.example.matching.api.service;

import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.MarketEvent;
import org.example.matching.api.dto.enums.EventStatus;
import org.example.matching.api.service.LiquidBotService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MarketManagmentService {
    private final WalletService walletService;
    private final LiquidBotService liquidBotService;

    // Stores Event by ID
    private final Map<String, MarketEvent> events = new ConcurrentHashMap<>();
    // Stores Ticker Pairs (YES <-> NO)
    private final Map<String, String> tickerPairs = new ConcurrentHashMap<>();
    // NEW: Maps Tickers to the Event they belong to
    private final Map<String, MarketEvent> tickerToEvent = new ConcurrentHashMap<>();

    // Use constructor injection to avoid circular dependency issues
    public MarketManagmentService(WalletService walletService, @Lazy LiquidBotService liquidBotService) {
        this.walletService = walletService;
        this.liquidBotService = liquidBotService;
    }

    public void registerEvent(MarketEvent event) {
        tickerPairs.put(event.getYesTicker(), event.getNoTicker());
        tickerPairs.put(event.getNoTicker(), event.getYesTicker());

        // Map both tickers to this event object
        tickerToEvent.put(event.getYesTicker(), event);
        tickerToEvent.put(event.getNoTicker(), event);
    }

    public String getPartnerTicker(String ticker) {
        return tickerPairs.get(ticker);
    }

    public MarketEvent getEvent(String id) {
        return events.get(id);
    }

    public MarketEvent getEventByTicker(String ticker) {
        return tickerToEvent.get(ticker);
    }
    
    // Check if ticker belongs to an event (prediction market) vs regular stock
    public boolean isEventTicker(String ticker) {
        return tickerToEvent.containsKey(ticker);
    }

    public MarketEvent createEvent(String id, String question, String yesTicker, String noTicker, int minutesFromNow, Long liquidity) {
        // Use provided liquidity or default to 10000
        long eventLiquidity = (liquidity != null) ? liquidity : 10000L;
        
        MarketEvent event = MarketEvent.builder()
                .eventID(id)
                .questions(question)
                .yesTicker(yesTicker)
                .noTicker(noTicker)
                .expiry(LocalDateTime.now().plusMinutes(minutesFromNow))
                .status(EventStatus.OPEN)
                .liquidity(eventLiquidity) // Use configurable liquidity
                .build();

        events.put(id, event);
        registerEvent(event); // Populate the lookup maps

        // Admin can make a "mistake" here (e.g. 100,000) 
        // and it won't break the 50-50 start!
        walletService.creditUserShares("HOUSE_BOT", yesTicker, 100000);
        walletService.creditUserShares("HOUSE_BOT", noTicker, 100000);
        
        // Give bot cash to place BUY orders (for market making)
        walletService.creditUserCash("HOUSE_BOT", 10000000); // 100,000 dollars = 10,000,000 cents

        // Force the first quote at 50/50
        liquidBotService.updateMarketTrigger(yesTicker); 
        return event;
    }

    public java.util.Collection<MarketEvent> getAllOpenEvents() {
        return events.values().stream()
                .filter(event -> event.getStatus() == EventStatus.OPEN)
                .collect(java.util.stream.Collectors.toList());
    }
}