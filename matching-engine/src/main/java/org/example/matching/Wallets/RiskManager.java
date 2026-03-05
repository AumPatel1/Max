package org.example.matching.Wallets;

import lombok.AllArgsConstructor;
import org.example.matching.model.Order;
import org.example.matching.orderbook.OrderRepository;
import org.springframework.stereotype.Service;

@Service @AllArgsConstructor
public class RiskManager {

    private final WalletService walletService;
    private final OrderRepository orderRepository; // 1. Add the field
//    public RiskManager(WalletService walletService) {
//        this.walletService = walletService;
//    }


    public boolean checkAndReserve(Order order) {
        // currently just delegates to walletService which performs the reserve attempt
        //orderRepository.save(order);

        // 2. NOW ATTEMPT TO RESERVE
        return walletService.reserveForOrder(order);
    }
}