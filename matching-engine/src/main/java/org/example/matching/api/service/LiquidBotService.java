package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.matching.api.dto.OrderRequest;
import org.example.matching.api.dto.OrderResponse;
import org.example.matching.api.dto.MarketEvent;
import org.example.matching.matching.MatchingEngine;
import org.example.matching.model.OrderSide;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiquidBotService {
    
    private final OrderService orderService;
    private final MarketManagmentService marketManagmentService;
    private final MatchingEngine matchingEngine;
    
    private static final String BOT_ID = "HOUSE_BOT";
    private static final int SPREAD = 4;

    public void updateMarketTrigger(String ticker) {
        // 🎯 KEY CHECK: Only provide liquidity for prediction markets (events), not regular stocks
        if (!marketManagmentService.isEventTicker(ticker)) {
            log.info("Ticker {} is not an event ticker - bot will not provide liquidity", ticker);
            return;
        }
        
        MarketEvent event = marketManagmentService.getEventByTicker(ticker);
        if (event == null || event.getStatus() != org.example.matching.api.dto.enums.EventStatus.OPEN) {
            // Don't provide liquidity for settled events
            return;
        }

        // 1. Get the displacement (starts at 0)
        long netSold = event.getVirtualNetSold().get();
        long L = event.getLiquidity();

        // 2. SIGMOID MATH (Guaranteed 50¢ if netSold is 0)
        double exponent = (double) netSold / L;
        long fairPriceYes = (long) (100.0 / (1.0 + Math.exp(-exponent)));
        fairPriceYes = Math.max(1, Math.min(99, fairPriceYes));
        long fairPriceNo = 100 - fairPriceYes;

        // 3. SMART BOT LOGIC - Only provide liquidity if there's insufficient natural liquidity
        boolean needsLiquidity = shouldProvideLiquidity(ticker, fairPriceYes, fairPriceNo);
        
        if (needsLiquidity) {
            refreshBotOrders(event.getYesTicker(), fairPriceYes, true);
            refreshBotOrders(event.getNoTicker(), fairPriceNo, false);
            log.info("Bot providing liquidity for {}: YES {}¢, NO {}¢ (virtualNetSold: {})", 
                    ticker, fairPriceYes, fairPriceNo, netSold);
        } else {
            // Step back - cancel bot orders when there's natural liquidity
            matchingEngine.cancelAllOrdersForUser(BOT_ID, event.getYesTicker());
            matchingEngine.cancelAllOrdersForUser(BOT_ID, event.getNoTicker());
            log.info("Bot stepping back for {} - natural liquidity available", ticker);
        }
    }
    
    private boolean shouldProvideLiquidity(String ticker, long fairPriceYes, long fairPriceNo) {
        // Get both order books for the event
        String yesTicker = ticker.contains("_Y") ? ticker : ticker.replace("_N", "_Y");
        String noTicker = ticker.contains("_N") ? ticker : ticker.replace("_Y", "_N");
        
        var yesBook = matchingEngine.getOrderBookForMarketData(yesTicker);
        var noBook = matchingEngine.getOrderBookForMarketData(noTicker);
        
        boolean yesHasNaturalOrders = hasNaturalOrders(yesBook);
        boolean noHasNaturalOrders = hasNaturalOrders(noBook);
        
        log.info("Liquidity check for {} - YES has natural: {}, NO has natural: {}", 
                ticker, yesHasNaturalOrders, noHasNaturalOrders);
        
        // Only provide liquidity if there's insufficient natural liquidity on either side
        // But also consider if there are ANY natural orders at all
        boolean hasAnyNaturalOrders = yesHasNaturalOrders || noHasNaturalOrders;
        
        // Step back if there are natural orders, provide liquidity if market is empty
        return !hasAnyNaturalOrders;
    }
    
    private boolean hasNaturalOrders(org.example.matching.model.OrderBook book) {
        if (book.getBids().isEmpty() && book.getAsks().isEmpty()) {
            return false; // Empty book - need liquidity
        }
        
        // Check if there are orders from users other than bot
        boolean naturalBids = book.getBids().values().stream().anyMatch(orders -> 
                orders.stream().anyMatch(order -> !order.getUserId().equals(BOT_ID)));
        boolean naturalAsks = book.getAsks().values().stream().anyMatch(orders -> 
                orders.stream().anyMatch(order -> !order.getUserId().equals(BOT_ID)));
        
        log.info("Order book check - Natural bids: {}, Natural asks: {}, Total bid levels: {}, Total ask levels: {}", 
                naturalBids, naturalAsks, book.getBids().size(), book.getAsks().size());
        
        return naturalBids || naturalAsks;
    }

    // This is called by your OrderService AFTER a trade happens
    public void recordTrade(String ticker, long quantity, String side) {
        MarketEvent event = marketManagmentService.getEventByTicker(ticker);
        if (event == null) return;

        // If a human BUYS from the bot, displacement goes UP
        // If a human SELLS to the bot, displacement goes DOWN
        if (side.equalsIgnoreCase("BUY")) {
            event.getVirtualNetSold().addAndGet(quantity);
        } else {
            event.getVirtualNetSold().addAndGet(-quantity);
        }

        log.info("Recorded trade: {} {} {} shares (virtualNetSold: {})", 
                ticker, side, quantity, event.getVirtualNetSold().get());

        // Immediately trigger price update based on new displacement
        updateMarketTrigger(ticker);
    }

    private void refreshBotOrders(String ticker, long fairPrice, boolean isYesTicker) {
        // Cancel all existing bot orders for this ticker
        matchingEngine.cancelAllOrdersForUser(BOT_ID, ticker);

        // Place new orders with 1-cent spread around fair price for better visibility
        long bidPrice = Math.max(1, fairPrice - 1);
        long askPrice = Math.min(99, fairPrice + 1);

        // Place bid order (buy from users)
        placeBotOrder(ticker, OrderSide.BUY, bidPrice, 500);
        
        // Place ask order (sell to users)
        placeBotOrder(ticker, OrderSide.SELL, askPrice, 500);
    }

    private void placeBotOrder(String ticker, OrderSide side, long price, long quantity) {
        OrderRequest req = new OrderRequest();
        req.setUserId(BOT_ID);
        req.setInstrument(ticker);
        req.setSide(side.name());
        req.setPrice(price);
        req.setQuantity(quantity);
        req.setIdempotencyKey(BOT_ID + "-" + ticker + "-" + side + "-" + System.currentTimeMillis());
        
        try {
            orderService.processOrder(req);
        } catch (Exception e) {
            log.error("Failed to place bot order: {}", req, e);
        }
    }
}