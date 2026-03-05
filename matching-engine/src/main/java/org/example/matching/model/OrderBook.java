//package org.example.matching.model;
////Draft
//
//
//import com.sun.source.tree.BinaryTree;
//import com.sun.source.tree.ExpressionTree;
//import com.sun.source.tree.Tree;
//import com.sun.source.tree.TreeVisitor;
//
//import java.util.*;
//
//public class OrderBook {
//
//    private OrderSide orderSide;
//    private Order order;
//
//
//    //Balancer that balances the percentage when it comes
//    Long MoneyPool;
//    TreeMap<Long,Object> buy = new TreeMap<>();
//    TreeMap<Long,Object> sell = new TreeMap<>();
//    //or TreeMap<Long,Long> sell = new TreeMap<>();
////or TreeMap<BigDecimal, List<Order>> sellOrders = new TreeMap<>();
//       // sell.put
//    //get list of orders from db
//    //
//List<Object> trades = new ArrayList<>();
//// assume we a.add(sell)
//    //orderside che a = sell and buy
//    //buy list
//    //sell list
//    //order1 is order1dto
//    public Map<Long ,Object> Match(OrderSide a , TreeMap<Long,Object> buy , TreeMap<Long,Object> sell , Order order1){
//        // we get order1 from post req , order1dto ig , and take here
//        if (order1.side==OrderSide.BUY){
//            long price = order1.price;
//            long timestamp = order1.timestamp;
//            if(price>0){
//                //we need to have a list of buy and sell guys.
//                // putting in order before that we need the least of sell guys to see and sort so we need binary
//                Long bestask = sell.firstKey(); // the least value in tree
//                if(price>sell.firstKey()){
//                    if(order1.quantity==bestask.quantity){
//                        //sell all
//                    }
//                    if(//less quantity
//                    ){
//                        //only share those
//                    }
//                    trades.add(order1);
//                }
//                /*  if (price == //lest value of tree)
//              Long bestask = sell.firstKey();
//              sell.remove(bestask)
//              price
//                // )
//                //remove sell.remove generate a trade impact algorithm of percentage or stock // later topic
//               */
//
//                //1) Binary Search Tree
//                //2) get last value
//                //3) compare
//
//            }
//
//        }
//
//
//
//    }
//
//
//}
//

package org.example.matching.model;

import org.example.matching.journal.EventJournal;

import java.io.IOException;
import java.util.*;

public class OrderBook {

    private final TreeMap<Long,Deque<Order>> ask = new TreeMap<>();
    private final TreeMap<Long,Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    private final Map<String,Long> orderPriceIndex = new HashMap<>(); // orderId->price
    
    public OrderBook() {
    }
   public List<Trade> placeOrder(Order incoming) {
        List<Trade> trades = new ArrayList<>();
        
        if (incoming.getSide() == OrderSide.BUY) {
            matchBuy(incoming, trades);
            if (incoming.getQuantity() > 0) {
                addToBook(bids, incoming);
            }
        } else {
            matchSell(incoming, trades);
            for(Trade t:trades) {
                // Journal matches if needed
            }
            if (incoming.getQuantity() > 0) {
                addToBook(ask, incoming);
            }
        }
        return trades;
   }
    private void matchBuy(Order buy, List<Trade> trades) {
        while(buy.getQuantity()>0&& !ask.isEmpty()){
            //it is first seen if ask is not empty
            Map.Entry<Long,Deque<Order>> bestAskEntry = ask.firstEntry();
            //getting the root node of treemap i.e.top value of it .
            long askPrice = bestAskEntry.getKey();
            // the long will be price do .key() is askprice of that node

            if(askPrice> buy.getPrice()){
                System.out.println("your order will fullfill once it matches the appropriate seller ");
                        break;
            }
            Deque<Order> queue = bestAskEntry.getValue();
            // it will be a single oblject and not queue, why is it in queue jere as the first node is object
            //getting the first node
            Order sell = queue.peekFirst();
            if(sell==null){
                ask.remove(askPrice);
                continue;
            }
            long tradeQty = Math.min(buy.getQuantity(),sell.getQuantity());
            long tradePrice = sell.getPrice();
            long ts = System.currentTimeMillis();
            trades.add(new Trade(buy.getId(),sell.getId(),tradePrice,tradeQty,ts));

            buy.reduceQuantity(tradeQty);
            sell.reduceQuantity(tradeQty);

            if(sell.getQuantity()==0){
                queue.removeFirst();
                orderPriceIndex.remove(sell.getId());
                if(queue.isEmpty()) ask.remove(askPrice);
                else{
                    //paritally filled resting order remains
                }
            }
        }
    }
    private void matchSell(Order sell, List<Trade> trades) {
        while (sell.getQuantity() > 0 && !bids.isEmpty()) {
            Map.Entry<Long, Deque<Order>> bestBidEntry = bids.firstEntry(); // because bids is reverseOrder
            long bidPrice = bestBidEntry.getKey();
            // price crossing condition: best bid >= sell price
            if (bidPrice < sell.getPrice()) break;

            Deque<Order> queue = bestBidEntry.getValue();
            Order buy = queue.peekFirst();
            if (buy == null) {
                bids.remove(bidPrice);
                continue;
            }

            int tradedQty = Math.min(sell.getQuantity(), buy.getQuantity());
            long tradePrice = buy.getPrice(); // resting buy price (maker)
            long ts = System.currentTimeMillis();
            trades.add(new Trade(buy.getId(), sell.getId(), tradePrice, tradedQty, ts));
        //    a.createlogs(sell,new Trade(buy.getId(), sell.getId(), tradePrice, tradedQty, ts));
            sell.reduceQuantity(tradedQty);
            buy.reduceQuantity(tradedQty);

            if (buy.getQuantity() == 0) {
                queue.removeFirst();
                orderPriceIndex.remove(buy.getId());
                if (queue.isEmpty()) bids.remove(bidPrice);
            } else {
                // partial fill; buy stays
            }
        }
    }
    private void addToBook(TreeMap<Long,Deque<Order>> bookside, Order order){
       // treemap bookside and order order

        Deque<Order> q = bookside.computeIfAbsent(order.getPrice(),k->new ArrayDeque<>());
        q.addLast(order);
        orderPriceIndex.put(order.getId(), order.getPrice());}

    public boolean cancel(String orderId) {
        Long price = orderPriceIndex.get(orderId);
        if (price == null) return false;
        // try both sides (only one will contain it)
        Deque<Order> q = ask.get(price);
        if (q != null && removeFromQueue(q, orderId)) {
            if (q.isEmpty()) ask.remove(price);
            orderPriceIndex.remove(orderId);
            return true;
        }
        q = bids.get(price);
        if (q != null && removeFromQueue(q, orderId)) {
            if (q.isEmpty()) bids.remove(price);
            orderPriceIndex.remove(orderId);
            return true;
        }
        return false;
    }

    private boolean removeFromQueue(Deque<Order> q, String orderId) {
        // linear scan; for higher performance maintain node references
        Iterator<Order> it = q.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o.getId().equals(orderId)) {
                it.remove();
                return true;
            }
        }
        return false;
    }
    public Optional<Long> bestAsk() {
        return ask.isEmpty() ? Optional.empty() : Optional.of(ask.firstKey());
    }
    public Optional<Long> bestBid() {
        return bids.isEmpty() ? Optional.empty() : Optional.of(bids.firstKey());
    }

    // Add getters for MatchingEngine access
    public TreeMap<Long, Deque<Order>> getAsks() {
        return ask;
    }

    public TreeMap<Long, Deque<Order>> getBids() {
        return bids;
    }

    public String dumpBook() {
        StringBuilder sb = new StringBuilder();
        sb.append("ASKS:\n");
        for (Map.Entry<Long, Deque<Order>> e : ask.entrySet()) {
            sb.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
        }
        sb.append("BIDS:\n");
        for (Map.Entry<Long, Deque<Order>> e : bids.entrySet()) {
            sb.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }
}
