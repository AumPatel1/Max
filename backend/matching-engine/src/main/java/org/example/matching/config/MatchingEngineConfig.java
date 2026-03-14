package org.example.matching.config;

import org.example.matching.Wallets.InMemoryWalletService;
import org.example.matching.Wallets.RiskManager;
import org.example.matching.Wallets.WalletService;
import org.example.matching.journal.EventJournal;
import org.example.matching.matching.MatchingEngine;
import org.example.matching.orderbook.InMemoryOrderRepository;
import org.example.matching.orderbook.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MatchingEngineConfig {

    @Bean
    public EventJournal eventJournal() {
        return new EventJournal();
    }

    @Bean
    public OrderRepository orderRepository() {
        return new InMemoryOrderRepository();
    }

    @Bean
    public WalletService walletService(OrderRepository orderRepository) {
        return new InMemoryWalletService(orderRepository);
    }

    @Bean
    public RiskManager riskManager(WalletService walletService, OrderRepository orderRepository) {
        return new RiskManager(walletService, orderRepository);
    }

    @Bean
    public MatchingEngine matchingEngine(EventJournal eventJournal) {
        return new MatchingEngine(eventJournal);
    }
}
