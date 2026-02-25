package org.example.matching.Wallets;

import org.example.matching.model.Order;
import org.example.matching.model.Trade;

public interface WalletService {
    boolean reserveforOrder(Order order);
    void  releaseReservation(String Ooderid);
    void SettleTrade(Trade trade);


    }
