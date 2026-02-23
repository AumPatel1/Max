//package org.example.matching.matching;
//
//import org.example.matching.journal.EventJournal;
//import org.example.matching.model.Order;
//import org.example.matching.model.OrderBook;
//import org.example.matching.model.Trade;
//
//import java.util.List;
//
//public class MatchingEngine {
//
//    private final OrderBook orderBook;
//    private final EventJournal journal;
//
//    public MatchingEngine() {
//        this.orderBook = new OrderBook();
//        this.journal = new EventJournal();
//
//    }
//
//    public List<Trade> placeOrder(Order order) {
//
//        journal.append("ORDER " +
//                order.getId() + " " +
//                order.getUserId() + " " +
//
//                order.getPrice() + " " +
//                order.getQuantity()
//        +" "+ order.getTimestamp() + " "+ order.getSide());
//
//        List<Trade> trades = orderBook.placeOrder(order);
//
//        for (Trade trade : trades) {
//            journal.append("TRADE " +
//                    trade.getBuyOrderId() + " " +
//                    trade.getSellOrderId() + " " +
//                    trade.getPrice() + " " +
//                    trade.getQuantity()
//            +trade.getTimestamp());
//        }
//
//        return trades;
//    }
//
//    public String dumpBook() {
//        return orderBook.dumpBook();
//    }
//}


package org.example.matching.matching;

import org.example.matching.journal.EventJournal;
import org.example.matching.model.Order;
import org.example.matching.model.OrderBook;
import org.example.matching.model.Trade;

import java.util.List;

public class MatchingEngine {

    private final OrderBook orderBook;
    private final EventJournal journal;

    public MatchingEngine() {
        this.orderBook = new OrderBook();
        this.journal = new EventJournal();
    }

    // live mode records to journal; record=false used during replay
    public List<Trade> placeOrder(Order order, boolean record) {
        if (record) {
            journal.appendOrder(order.getId(), order.getUserId(), order.getSide().name(), order.getPrice(), order.getQuantity(), order.getTimestamp());
        }

        List<Trade> trades = orderBook.placeOrder(order);

        if (record) {
            for (Trade t : trades) {
                journal.appendTrade(t.getBuyOrderId(), t.getSellOrderId(), t.getPrice(), (int) t.getQuantity(), t.getTimestamp());
            }
        }
        return trades;
    }

    // convenience for normal usage
    public List<Trade> placeOrder(Order order) {
        return placeOrder(order, true);
    }

    // used by replay to rebuild book without re-journaling
    public void replayOrder(Order order) {
        placeOrder(order, false);
    }

    // replay all lines from journal (optional helper)
    public void replayJournal() {
        List<String> lines = journal.readAllLines();
        for (String raw : lines) {
            if (raw == null || raw.isBlank()) continue;
            String[] parts = raw.split("\\s+");
            if (parts.length < 2) continue;
            if ("ORDER".equals(parts[0]) && parts.length >= 7) {
                // ORDER id user side price qty ts
                String id = parts[1];
                String user = parts[2];
                String side = parts[3];
                long price = Long.parseLong(parts[4]);
                int qty = Integer.parseInt(parts[5]);
                long ts = Long.parseLong(parts[6]);
                // create order via your Order constructor (check arg ordering)
                Order o = new Order(id, user, price, qty, ts, org.example.matching.model.OrderSide.valueOf(side));
                replayOrder(o);
            }
            // ignore TRADE lines during replay
        }
    }

    public String dumpBook() {
        return orderBook.dumpBook();
    }
}
