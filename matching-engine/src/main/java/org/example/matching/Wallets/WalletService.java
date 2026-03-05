package org.example.matching.Wallets;

import org.example.matching.model.Order;
import org.example.matching.model.Trade;
import org.example.matching.model.Wallet;

public interface WalletService {
    boolean reserveForOrder(Order order);
    void releaseReservation(String orderId);
    void settleTrade(Trade trade);
    
    // Additional methods needed for testing
    void creditUserShares(String userId, long shares);
    void creditUserCash(String userId, long cash);
    Wallet getWallet(String userId);
}
