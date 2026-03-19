package org.example.matching.orderbook;//package org.example.matching.orderbook;
//
//import org.example.matching.journal.EventJournal;
//import org.example.matching.matching.MatchingEngine;
//import org.example.matching.model.Order;
//import org.example.matching.model.Trade;
//

import org.example.matching.Wallets.InMemoryWalletService;
import org.example.matching.Wallets.RiskManager;
import org.example.matching.Wallets.WalletService;
import org.example.matching.journal.EventJournal;
import org.example.matching.matching.MatchingEngine;
import org.example.matching.model.Order;
import org.example.matching.model.Trade;
import org.example.matching.model.Wallet;
import org.example.matching.orderbook.OrderRepository;
import org.example.matching.validation.OrderValidator;

import java.util.List;

public class OrderOrchestrator {

    private final MatchingEngine matchingEngine;
    private final EventJournal eventJournal;
    private final OrderValidator orderValidator;
    private final RiskManager riskManager;
    private final OrderRepository orderRepository;
    private final WalletService walletService;

    // Constructor for basic functionality
    public OrderOrchestrator(MatchingEngine matchingEngine,
                             EventJournal eventJournal, OrderValidator orderValidator) {
        this.matchingEngine = matchingEngine;
        this.eventJournal = eventJournal;
        this.orderValidator = orderValidator;
        this.riskManager = null;
        this.orderRepository = null;
        this.walletService = null;
    }

    // Full constructor with wallet and risk management
    public OrderOrchestrator(MatchingEngine matchingEngine,
                             EventJournal eventJournal, 
                             RiskManager riskManager, 
                             OrderRepository orderRepository, 
                             WalletService walletService) {
        this.matchingEngine = matchingEngine;
        this.eventJournal = eventJournal;
        this.riskManager = riskManager;
        this.orderRepository = orderRepository;
        this.walletService = walletService;
        this.orderValidator = new OrderValidator(); // Default validator
    }

    public void submitOrder(Order order) {
        if (!orderValidator.validate(order)) {
            eventJournal.appendRaw("REJECT " + order.getId() + " INVALID_ORDER");
            return;
        }

        // Use risk manager and wallet service if available
        if (riskManager != null && walletService != null) {
            // Check and reserve funds/shares
            if (!riskManager.checkAndReserve(order)) {
                eventJournal.appendRaw("REJECT " + order.getId() + " INSUFFICIENT_FUNDS");
                return;
            }
            
            // Save order to repository
            orderRepository.save(order);
        }

        // 1️⃣ Journal the incoming order
        eventJournal.appendRaw(formatOrder(order));

        // 2️⃣ Send to matching engine
        List<Trade> trades = matchingEngine.placeOrder(order);

        // 3️⃣ Settle trades and journal results
        if (walletService != null) {
            for (Trade trade : trades) {
                walletService.settleTrade(trade);
                eventJournal.appendRaw(formatTrade(trade));
            }
        } else {
            // Just journal trades if no wallet service
            for (Trade trade : trades) {
                eventJournal.appendRaw(formatTrade(trade));
            }
        }
    }

    private String formatOrder(Order order) {
        return String.format(
                "ORDER %s %s %s %d %d",
                order.getId(),
                order.getUserId(),
                order.getSide(),
                order.getPrice(),
                order.getQuantity()
        );
    }

    private String formatTrade(Trade trade) {
        return String.format(
                "TRADE %s %s %d %d",
                trade.getBuyOrderId(),
                trade.getSellOrderId(),
                trade.getPrice(),
                trade.getQuantity()
        );
    }
}