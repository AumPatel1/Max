package org.example.matching.Wallets;

import lombok.RequiredArgsConstructor;
import org.example.matching.entity.User;
import org.example.matching.entity.Trade;
import org.example.matching.model.Order;
import org.example.matching.model.Reservation;
import org.example.matching.model.Wallet;
import org.example.matching.orderbook.OrderRepository;
import org.example.matching.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class HybridWalletService implements WalletService {

    // In-memory for fast access
    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    
    // Database for persistence
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final String INSTRUMENT = "MARKET";

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
    public void releaseReservation(String orderId) {
        Reservation r = reservations.remove(orderId);
        if (r == null) return;
        
        Wallet w = ensureWallet(r.getUserId());
        if (r.getIsBuy()) {
            w.releaseReserveCash(r.getReservedCash());
        } else {
            w.releaseReservedShares(r.getInstrument(), r.getReservedShares());
        }
    }

    @Override
    public void releaseAllReservationsForInstrument(String instrument) {
        reservations.values().stream()
                .filter(r -> instrument.equals(r.getInstrument()))
                .map(Reservation::getOrderId)
                .collect(java.util.stream.Collectors.toList())
                .forEach(this::releaseReservation);
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
            long cashToDebitFromReserved = buyRes.reduceBy(qty);
            buyerWallet.debitReservedCash(cashToDebitFromReserved);

            long refund = cashToDebitFromReserved - tradeValue;
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
        
        // Sync to database
        syncUserToDatabase(buy.getUserId());
        syncUserToDatabase(sell.getUserId());
    }

    @Override
    public void creditUserShares(String userId, long shares) {
        Wallet w = ensureWallet(userId);
        w.addAvailableShares(INSTRUMENT, shares);
        syncUserToDatabase(userId);
    }

    @Override
    public void creditUserShares(String userId, String instrument, long shares) {
        Wallet w = ensureWallet(userId);
        w.addAvailableShares(instrument, shares);
        syncUserToDatabase(userId);
    }

    @Override
    public void creditUserCash(String userId, long cash) {
        Wallet w = ensureWallet(userId);
        w.addAvailableCash(cash);
        syncUserToDatabase(userId);
    }

    @Override
    public Wallet getWallet(String userId) {
        return wallets.get(userId);
    }

    @Override
    public Collection<Wallet> getAllWallets() {
        return wallets.values();
    }

    // Helper methods
    private Wallet ensureWallet(String userId) {
        // Try to load from database first
        if (!wallets.containsKey(userId)) {
            try {
                User dbUser = userRepository.findById(Long.valueOf(userId));
                if (dbUser != null) {
                    // Convert database user to in-memory wallet
                    Wallet wallet = new Wallet(userId);
                    wallet.addAvailableCash(dbUser.getBalance().longValue());
                    wallets.put(userId, wallet);
                }
            } catch (Exception e) {
                // User not found in database, create new wallet
            }
        }
        return wallets.computeIfAbsent(userId, k -> new Wallet(userId));
    }

    @Transactional
    private void syncUserToDatabase(String userId) {
        try {
            Wallet wallet = wallets.get(userId);
            if (wallet != null) {
                User dbUser = userRepository.findById(Long.valueOf(userId))
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setUsername(userId);
                            newUser.setEmail(userId + "@example.com");
                            newUser.setPasswordHash("temp_hash");
                            return userRepository.save(newUser);
                        });
                
                dbUser.setBalance(BigDecimal.valueOf(wallet.getAvailableCash()));
                userRepository.save(dbUser);
            }
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to sync user to database: " + e.getMessage());
        }
    }
}
