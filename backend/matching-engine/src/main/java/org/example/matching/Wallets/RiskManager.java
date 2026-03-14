package org.example.matching.Wallets;

import org.example.matching.model.Order;
import org.example.matching.orderbook.OrderRepository;

public class RiskManager {

    private final WalletService walletService;
    private final OrderRepository orderRepository; // 1. Add the field

    // No-arg constructor for Main.java usage
    public RiskManager() {
        this.walletService = null;
        this.orderRepository = null;
    }

    // Constructor for dependency injection
    public RiskManager(WalletService walletService, OrderRepository orderRepository) {
        this.walletService = walletService;
        this.orderRepository = orderRepository;
    }


    public boolean checkAndReserve(Order order) {
        // currently just delegates to walletService which performs the reserve attempt
        //orderRepository.save(order);

        // 2. NOW ATTEMPT TO RESERVE
        return walletService.reserveForOrder(order);
    }
}