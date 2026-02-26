package org.example.matching.orderbook;//package org.example.matching.orderbook;
//
//import org.example.matching.journal.EventJournal;
//import org.example.matching.matching.MatchingEngine;
//import org.example.matching.model.Order;
//import org.example.matching.model.Trade;
//
//import java.util.List;
//




     //   import org.example.matching.engine.MatchingEngine;
        import org.example.matching.Wallets.InMemoryWalletService;
        import org.example.matching.Wallets.RiskManager;
        import org.example.matching.journal.EventJournal;
        import org.example.matching.matching.MatchingEngine;
        import org.example.matching.model.Order;
        import org.example.matching.model.Trade;
        import org.example.matching.validation.OrderValidator;

        import java.util.List;

public class OrderOrchestrator {

    private final MatchingEngine matchingEngine;
    private final EventJournal eventJournal;
    private final OrderValidator orderValidator;

    //order orchestrator

    public OrderOrchestrator(MatchingEngine matchingEngine,
                             EventJournal eventJournal,OrderValidator orderValidator) {
        this.matchingEngine = matchingEngine;
        this.eventJournal = eventJournal;
        this.orderValidator= orderValidator;
    }
//
//    public OrderOrchestrator(MatchingEngine engine, EventJournal journal, RiskManager riskManager, OrderRepository orderRepo, InMemoryWalletService walletService) {
//    }

    public void submitOrder(Order order) {
        if (!orderValidator.validate(order)) {
            eventJournal.appendRaw("REJECT " + order.getId() + " INVALID_ORDER");
            return;
        }

        // 1️⃣ Journal the incoming order
        eventJournal.appendRaw(formatOrder(order));

        // 2️⃣ Send to matching engine
        List<Trade> trades = matchingEngine.placeOrder(order);

        // 3️⃣ Journal resulting trades
        for (Trade trade : trades) {
            eventJournal.appendRaw(formatTrade(trade));
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