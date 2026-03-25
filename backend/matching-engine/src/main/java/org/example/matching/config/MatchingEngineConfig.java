package org.example.matching.config;

import org.example.matching.Wallets.InMemoryWalletService;
import org.example.matching.Wallets.RiskManager;
import org.example.matching.Wallets.WalletService;
import org.example.matching.Wallets.HybridWalletService;
import org.example.matching.journal.EventJournal;
import org.example.matching.matching.MatchingEngine;
import org.example.matching.orderbook.InMemoryOrderRepository;
import org.example.matching.orderbook.OrderRepository;
import org.example.matching.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "org.example.matching.repository")
public class MatchingEngineConfig {

    private final UserRepository userRepository;

    public MatchingEngineConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
        return new HybridWalletService(userRepository, orderRepository);
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
