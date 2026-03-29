package org.example.matching.Wallets;

import org.example.matching.model.Order;
import org.example.matching.model.Trade;
import org.example.matching.model.Wallet;

import java.util.Collection;

public interface WalletService {
    boolean reserveForOrder(Order order);
    void releaseReservation(String orderId);
    void settleTrade(Trade trade);

    // Additional methods needed for testing and API

    /**
     * Creates an empty wallet for the given userId if one does not already exist.
     * Called by AuthController during user registration to guarantee every user
     * has a wallet before they can place orders or deposit funds.
     */
    void createWallet(String userId);

    void creditUserShares(String userId, long shares);
    void creditUserShares(String userId, String instrument, long shares);
    void creditUserCash(String userId, long cash);
    Wallet getWallet(String userId);
    Collection<Wallet> getAllWallets();
    void zeroOutShares(String userId, String instrument);
}
