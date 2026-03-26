package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.Wallets.RiskManager;
import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.OrderMapper;
import org.example.matching.api.dto.OrderRequest;
import org.example.matching.api.dto.OrderResponse;
import org.example.matching.entity.TradeEntity;
import org.example.matching.journal.EventJournal;
import org.example.matching.matching.MatchingEngine;
import org.example.matching.model.Order;
import org.example.matching.model.OrderSide;
import org.example.matching.model.Trade;
import org.example.matching.orderbook.OrderRepository;
import org.example.matching.Repository.OrderJpaRepository;
import org.example.matching.Repository.TradeJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private final OrderJpaRepository orderJpaRepo;
    private final TradeJpaRepository tradeJpaRepo;

    private final Map<String, OrderResponse> idempotencyStore = new ConcurrentHashMap<>();

    public OrderResponse processOrder(OrderRequest request) {
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey != null && idempotencyStore.containsKey(idempotencyKey)) {
            return idempotencyStore.get(idempotencyKey);
        }

        Order order = OrderMapper.toDomain(request);

        if (!riskManager.checkAndReserve(order)) {
            if (order.getSide() == OrderSide.SELL) {
                String counterpart = eventTickerRegistry.getCounterpart(order.getInstrument());
                if (counterpart != null) {
                    long complementaryPrice = 100L - order.getPrice();
                    if (complementaryPrice > 0) {
                        Order converted = new Order(
                                order.getId(), order.getUserId(),
                                complementaryPrice, order.getQuantity(),
                                order.getTimestamp(), OrderSide.BUY, counterpart);
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

        // Persist order to DB before placing in engine
        orderRepository.save(order);
        eventJournal.appendRaw("ORDER_PLACED: " + order.getId());

        List<Trade> trades = matchingEngine.placeOrder(order);

        for (Trade trade : trades) {
            walletService.settleTrade(trade);
            marketDataService.onTrade(order.getInstrument(), trade);
            eventJournal.appendRaw("TRADE_SETTLED: " + trade.getBuyOrderId() + " <-> " + trade.getSellOrderId());

            // Persist trade
            tradeJpaRepo.save(TradeEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .buyOrderId(trade.getBuyOrderId())
                    .sellOrderId(trade.getSellOrderId())
                    .instrument(order.getInstrument())
                    .price(trade.getPrice())
                    .quantity(trade.getQuantity())
                    .timestamp(trade.getTimestamp())
                    .build());

            // Update resting order's remaining qty in DB
            String restingId = (order.getSide() == OrderSide.BUY)
                    ? trade.getSellOrderId() : trade.getBuyOrderId();
            orderJpaRepo.decrementRemainingQty(restingId, (int) trade.getQuantity());
            // Check if fully filled
            orderJpaRepo.findById(restingId).ifPresent(e -> {
                if (e.getRemainingQty() - (int) trade.getQuantity() <= 0) {
                    orderJpaRepo.updateStatus(restingId, "FILLED");
                }
            });
        }

        // Update incoming order status in DB
        if (!trades.isEmpty()) {
            String newStatus = (order.getQuantity() == 0) ? "FILLED" : "PARTIALLY_FILLED";
            orderJpaRepo.updateRemainingQtyAndStatus(order.getId(), order.getQuantity(), newStatus);
        }

        var book = matchingEngine.getOrderBookForMarketData(order.getInstrument());
        marketDataService.updateBookTops(order.getInstrument(), book.getBestBid(), book.getBestAsk());
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
