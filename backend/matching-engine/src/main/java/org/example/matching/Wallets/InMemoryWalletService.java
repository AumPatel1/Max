package org.example.matching.Wallets;

import lombok.RequiredArgsConstructor;
import org.example.matching.model.Order;
import org.example.matching.model.Reservation;
import org.example.matching.model.Trade;
import org.example.matching.model.Wallet;
import org.example.matching.orderbook.OrderRepository;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryWalletService implements WalletService {

    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private final OrderRepository orderRepository;
    private final String INSTRUMENT = "MARKET";

    // No-arg constructor for Main.java usage
    public InMemoryWalletService() {
        this.orderRepository = null;
    }

    // Constructor for dependency injection
    public InMemoryWalletService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private Wallet ensureWallet(String userId) {
        return wallets.computeIfAbsent(userId, k -> new Wallet(userId));
    }

    @Override
    public boolean reserveForOrder(Order order) {
        String userId = order.getUserId();
        String instrument = order.getInstrument();
        Wallet w = ensureWallet(userId);

        if (order.getSide().name().equals("BUY")) {
            long required = order.getPrice() * (long) order.getQuantity();
            if (!w.tryReserveCash(required)) return false;

            Reservation r = new Reservation(order.getId(), userId, order.getPrice(), order.getQuantity(), true, instrument);
            r.setReservedCash(required);
            reservations.put(order.getId(), r);
        } else {
            if (!w.tryReserveShares(instrument, order.getQuantity())) return false;

            Reservation r = new Reservation(order.getId(), userId, order.getPrice(), order.getQuantity(), false, instrument);
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
            w.releaseReservedShares(r.getInstrument(),r.getReservedShares());

        }
    }

    @Override
    public void creditUserShares(String userId, long shares) {
        Wallet w = ensureWallet(userId);
        w.addAvailableShares(INSTRUMENT, shares);
    }

    @Override
    public void creditUserShares(String userId, String instrument, long shares) {
        Wallet w = ensureWallet(userId);
        w.addAvailableShares(instrument, shares);
    }

    @Override
    public void creditUserCash(String userId, long cash) {
        Wallet w = ensureWallet(userId);
        w.addAvailableCash(cash);
    }

    @Override
    public Wallet getWallet(String userId) {
        return wallets.get(userId);
    }

    @Override
    public Collection<Wallet> getAllWallets() {
        return wallets.values();
    }

    @Override
    public void zeroOutShares(String userId, String instrument) {
        Wallet w = wallets.get(userId);
        if (w == null) return;
        long avail    = w.getAvailableShares(instrument);
        long reserved = w.getReservedShares(instrument);
        if (avail    > 0) w.getAvailableShares().computeIfAbsent(instrument, k -> new java.util.concurrent.atomic.AtomicLong(0)).addAndGet(-avail);
        if (reserved > 0) w.getReservedShares().computeIfAbsent(instrument,  k -> new java.util.concurrent.atomic.AtomicLong(0)).addAndGet(-reserved);
    }

    @Override
    public void settleTrade(Trade trade) {
        Order buy = orderRepository.findById(trade.getBuyOrderId()).orElse(null);
        Order sell = orderRepository.findById(trade.getSellOrderId()).orElse(null);

        if (buy == null || sell == null) return;

        Wallet buyerWallet = ensureWallet(buy.getUserId());
        Wallet sellerWallet = ensureWallet(sell.getUserId());
        Reservation buyRes = reservations.get(buy.getId());
        Reservation sellRes = reservations.get(sell.getId());

        int qty = (int) trade.getQuantity();
        long tradeValue = trade.getPrice() * (long) qty;
        String instrument = buy.getInstrument();
        // Process Buy Side
        if (buyRes != null) {
            long cashtoDebitFromReserved = buyRes.reduceBy(qty);
            buyerWallet.debitReservedCash(cashtoDebitFromReserved);

            long refund = cashtoDebitFromReserved - tradeValue;
            if (refund > 0) buyerWallet.addAvailableCash(refund);

            buyerWallet.addAvailableShares(instrument, qty);
        }

        // Process Sell Side
        if (sellRes != null) {
            sellRes.reduceBy(qty);
            sellerWallet.debitReservedShares(instrument, (long) qty);
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