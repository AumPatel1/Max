package org.example.matching.Wallets;

import lombok.RequiredArgsConstructor;
import org.example.matching.model.Order;
import org.example.matching.model.Reservation;
import org.example.matching.model.Trade;
import org.example.matching.model.Wallet;
import org.example.matching.orderbook.OrderRepository;
import org.springframework.stereotype.Service; // Add this import

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service // Tells Spring this is a managed bean
@RequiredArgsConstructor
public class InMemoryWalletService implements WalletService {

    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private final OrderRepository orderRepository;
    private final String INSTRUMENT = "MARKET";

    private Wallet ensureWallet(String userId) {
        return wallets.computeIfAbsent(userId, k -> new Wallet(userId));
    }

    @Override
    public boolean reserveforOrder(Order order) {
        String userId = order.getUserId();
        Wallet w = ensureWallet(userId);

        if (order.getSide().name().equals("BUY")) {
            long required = order.getPrice() * (long) order.getQuantity();
            if (!w.tryReserveCash(required)) return false;

            Reservation r = new Reservation(order.getId(), userId, order.getPrice(), order.getQuantity(), true);
            r.setReservedCash(required);
            reservations.put(order.getId(), r);
        } else {
            if (!w.tryReserveShares(INSTRUMENT, order.getQuantity())) return false;

            Reservation r = new Reservation(order.getId(), userId, order.getPrice(), order.getQuantity(), false);
            r.setReservedShares(order.getQuantity());
            reservations.put(order.getId(), r);
        }
        return true;
    }


@Override
public void releaseReservation(String orderId){
        Reservation r = reservations.remove(orderId);
        if(r==null)return;
        Wallet w = ensureWallet(r.getUserId());
        if(r.getIsBuy()){
            w.releaseReserveCash(r.getReservedCash());

        }else{
            w.releaseReservedShares(INSTRUMENT,r.getReservedShares());

        }


}




    @Override
    public void SettleTrade(Trade trade) {
        Order buy = orderRepository.findById(trade.getBuyOrderId()).orElse(null);
        Order sell = orderRepository.findById(trade.getSellOrderId()).orElse(null);

        if (buy == null || sell == null) return;

        Wallet buyerWallet = ensureWallet(buy.getUserId());
        Wallet sellerWallet = ensureWallet(sell.getUserId());
        Reservation buyRes = reservations.get(buy.getId());
        Reservation sellRes = reservations.get(sell.getId());

        int qty = (int) trade.getQuantity();
        long tradeValue = trade.getPrice() * (long) qty;

        // Process Buy Side
        if (buyRes != null) {
            long cashtoDebitFromReserved = buyRes.reduceBy(qty);
            buyerWallet.debitReservedCash(cashtoDebitFromReserved);

            long refund = cashtoDebitFromReserved - tradeValue;
            if (refund > 0) buyerWallet.addAvailableCash(refund);

            buyerWallet.addAvailableShares(INSTRUMENT, qty);
        }

        // Process Sell Side
        if (sellRes != null) {
            sellRes.reduceBy(qty);
            sellerWallet.debitReservedShares(INSTRUMENT, (long) qty);
            sellerWallet.addAvailableCash(tradeValue);
        }

        // Cleanup completed reservations
        if (buyRes != null && buyRes.getRemainingQty() == 0) {
            reservations.remove(buy.getId());
        }
        if (sellRes != null && sellRes.getRemainingQty() == 0) {
            reservations.remove(sell.getId());
        }
    }
}