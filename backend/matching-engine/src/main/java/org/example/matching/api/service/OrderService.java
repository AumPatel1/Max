package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.Wallets.RiskManager;
import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.OrderMapper;
import org.example.matching.api.dto.OrderRequest;
import org.example.matching.api.dto.OrderResponse;
import org.example.matching.journal.EventJournal;
import org.example.matching.matching.MatchingEngine;
import org.example.matching.model.Order;
import org.example.matching.model.OrderSide;
import org.example.matching.model.Trade;
import org.example.matching.orderbook.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MarketDataService marketDataService;
    private final RiskManager riskManager;
    private final MatchingEngine matchingEngine;
    private final WalletService walletService;
    private final EventJournal eventJournal;
    private final OrderRepository orderRepository;
    private final EventTickerRegistry eventTickerRegistry;

    // In-memory idempotency store: Key -> Previous Response
    private final Map<String, OrderResponse> idempotencyStore = new ConcurrentHashMap<>();

    public OrderResponse processOrder(OrderRequest request) {
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey != null && idempotencyStore.containsKey(idempotencyKey)) {
            return idempotencyStore.get(idempotencyKey);
        }

        Order order = OrderMapper.toDomain(request);

        if (!riskManager.checkAndReserve(order)) { //First tries normal validation (cash for BUY, shares for SELL)
            // For event tickers: selling a side you don't hold = buying the opposite side.
            // e.g. SELL NO (no shares) → BUY YES at (100 - price), and vice versa.
            if (order.getSide() == OrderSide.SELL) { //Only SELL orders get converted to synthetic positions
                String counterpart = eventTickerRegistry.getCounterpart(order.getInstrument());//Key Formula: YES price = 100 - NO price
               // Example: SELL NO at 30c → BUY YES at 70c
                //Validates price > 0 (line 51)
                if (counterpart != null) {
                    long complementaryPrice = 100L - order.getPrice();
                    if (complementaryPrice > 0) {
                        Order converted = new Order(
                                order.getId(), order.getUserId(),
                                complementaryPrice, order.getQuantity(),
                                order.getTimestamp(), OrderSide.BUY, counterpart //// Changed to BUY
                        );
                        if (!riskManager.checkAndReserve(converted)) {
                            return buildResponse(order, "REJECTED", "Insufficient funds or shares");
                        }
                        order = converted;
                    } else {
                        return buildResponse(order, "REJECTED", "Insufficient funds or shares");
                    }
                } else {
                    return buildResponse(order, "REJECTED", "Insufficient funds or shares");
                }
            } else {
                return buildResponse(order, "REJECTED", "Insufficient funds or shares");
            }
        }

        orderRepository.save(order);
        eventJournal.appendRaw("ORDER_PLACED: " + order.getId());

        // Matching Engine execution
        List<Trade> trades = matchingEngine.placeOrder(order);

        for (Trade trade : trades) {
            // so once trade is done manage the cash and shares of the users using their ids and stuff in walletService below
            walletService.settleTrade(trade);
            marketDataService.onTrade(order.getInstrument(), trade);  // updates stats + pushes WS trade event
            eventJournal.appendRaw("TRADE_SETTLED: " + trade.getBuyOrderId() + " <-> " + trade.getSellOrderId());
        }
        var book = matchingEngine.getOrderBookForMarketData(order.getInstrument());
        marketDataService.updateBookTops(
                order.getInstrument(),
                book.getBestBid(),
                book.getBestAsk()
        );
        // Push book_update diff to WS subscribers after all trades are settled
        marketDataService.onBookChange(order.getInstrument(), matchingEngine.getSnapshot(order.getInstrument()));

        OrderResponse response = buildResponse(order, "ACCEPTED", "Success");
        if (idempotencyKey != null) {
            idempotencyStore.put(idempotencyKey, response);
        }
        return response;
    }
    
    private OrderResponse buildResponse(Order order, String status, String msg) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .status(status)
                .message(msg)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

