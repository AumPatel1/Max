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
import org.example.matching.model.Trade;
import org.example.matching.orderbook.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final RiskManager riskManager;
    private final MatchingEngine matchingEngine;
    private final WalletService walletService;
    private final EventJournal eventJournal;
    private final OrderRepository orderRepository;

    // In-memory idempotency store: Key -> Previous Response
    private final Map<String, OrderResponse> idempotencyStore = new ConcurrentHashMap<>();

    public OrderResponse processOrder(OrderRequest request) {
        if (idempotencyStore.containsKey(request.getIdempotencyKey())) {
            return idempotencyStore.get(request.getIdempotencyKey());
        }
        
        Order order = OrderMapper.toDomain(request);
        
        if (!riskManager.checkAndReserve(order)) {
            return buildResponse(order, "REJECTED", "Insufficient funds or shares");
        }
        
        eventJournal.appendRaw("ORDER_PLACED: " + order.getId());

        // Matching Engine execution
        List<Trade> trades = matchingEngine.placeOrder(order);

        for (Trade trade : trades) {
            walletService.settleTrade(trade);
            eventJournal.appendRaw("TRADE_SETTLED: " + trade.getBuyOrderId() + " <-> " + trade.getSellOrderId());
        }

        OrderResponse response = buildResponse(order, "ACCEPTED", "Success");
        idempotencyStore.put(request.getIdempotencyKey(), response);
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

