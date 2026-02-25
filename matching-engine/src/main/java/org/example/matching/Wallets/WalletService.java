package org.example.matching.Wallets;

import org.example.matching.model.Order;
import org.example.matching.model.Trade;

public interface WalletService {
    boolean reserveforOrder(Order order);
    void  releaseReservation(String Oderid);
    void SettleTrade(Trade trade);


    }
