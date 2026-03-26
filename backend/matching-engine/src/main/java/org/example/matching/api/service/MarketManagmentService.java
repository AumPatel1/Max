package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.EventStatus;
import org.example.matching.api.dto.MarketEvent;
import org.example.matching.entity.MarketEventEntity;
import org.example.matching.repository.MarketEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketManagmentService {

    private final LiquidBotService liquidBotService;
    private final WalletService walletService;
    private final EventTickerRegistry eventTickerRegistry;
    private final MarketEventRepository eventRepo;

    @Transactional
    public MarketEvent createEvent(String id, String question, String yesTicker, String noTicker, int expiry) {
        // Return existing event if already created (idempotent)
        if (eventRepo.existsById(id)) {
            return toDto(eventRepo.findById(id).get());
        }

        // Fund HOUSE_BOT and seed liquidity
        walletService.creditUserCash("HOUSE_BOT", 100_000L);
        walletService.creditUserShares("HOUSE_BOT", yesTicker, 2000L);
        walletService.creditUserShares("HOUSE_BOT", noTicker, 2000L);

        // Persist event
        MarketEventEntity entity = MarketEventEntity.builder()
                .id(id).question(question)
                .yesTicker(yesTicker).noTicker(noTicker)
                .expiry(expiry).status("OPEN")
                .build();
        eventRepo.save(entity);

        // Register ticker pair for synthetic shorts
        eventTickerRegistry.registerPair(yesTicker, noTicker);

        // Seed initial order book liquidity
        liquidBotService.seedMarket(yesTicker, noTicker);

        return toDto(entity);
    }

    public MarketEvent getEvent(String id) {
        return eventRepo.findById(id).map(this::toDto).orElse(null);
    }

    @Transactional
    public void closeEvent(String id) {
        eventRepo.updateStatus(id, "CLOSED");
    }

    @Transactional
    public void settleEvent(String id) {
        eventRepo.updateStatus(id, "SETTLED");
    }

    private MarketEvent toDto(MarketEventEntity e) {
        return MarketEvent.builder()
                .id(e.getId())
                .question(e.getQuestion())
                .yesTicker(e.getYesTicker())
                .noTicker(e.getNoTicker())
                .expiry((int) e.getExpiry())
                .status(EventStatus.valueOf(e.getStatus()))
                .build();
    }
}
