package org.example.matching.config;

import org.example.matching.Wallets.RiskManager;
import org.example.matching.Wallets.WalletService;
import org.example.matching.journal.EventJournal;
import org.example.matching.matching.MatchingEngine;
import org.example.matching.orderbook.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MatchingEngineConfig {

    // DatabaseWalletService and DatabaseOrderRepository are @Service/@Repository
    // beans — Spring autowires them everywhere automatically.
    // This config only creates beans that have no Spring stereotype annotation.

    @Bean
    public EventJournal eventJournal() {
        return new EventJournal();
    }

    @Bean
    public MatchingEngine matchingEngine(EventJournal eventJournal) {
        return new MatchingEngine(eventJournal);
    }

    @Bean
    public RiskManager riskManager(WalletService walletService, OrderRepository orderRepository) {
        return new RiskManager(walletService, orderRepository);
    }
}
