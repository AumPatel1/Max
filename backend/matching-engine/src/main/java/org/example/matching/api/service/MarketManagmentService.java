package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.EventStatus;
import org.example.matching.api.dto.MarketEvent;
import org.example.matching.entity.MarketEventEntity;
import org.example.matching.Repository.MarketEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketManagmentService {

    private final LiquidBotService liquidBotService;
    private final WalletService walletService;
    private final EventTickerRegistry eventTickerRegistry;
    private final MarketEventRepository eventRepo;

    @Transactional
    public MarketEvent createEvent(String id, String question, String yesTicker, String noTicker, int expiry) {
        // hit the same endpoint twice? just return what's already there
        if (eventRepo.existsById(id)) {
            return toDto(eventRepo.findById(id).get());
        }

        // everything keys by uppercase internally — normalize here so nothing slips through lowercase
        yesTicker = yesTicker.toUpperCase();
        noTicker  = noTicker.toUpperCase();

        // give the house bot enough capital to quote both sides from the start
        walletService.creditUserCash("HOUSE_BOT", 100_000L);
        walletService.creditUserShares("HOUSE_BOT", yesTicker, 2000L);
        walletService.creditUserShares("HOUSE_BOT", noTicker, 2000L);

        MarketEventEntity entity = MarketEventEntity.builder()
                .id(id).question(question)
                .yesTicker(yesTicker).noTicker(noTicker)
                .expiry(expiry).status("OPEN")
                .build();
        eventRepo.save(entity);

        // register the YES/NO pair so the engine knows they're two sides of the same market
        eventTickerRegistry.registerPair(yesTicker, noTicker);

        // drop initial bids/asks so the book isn't empty when the first real user arrives
        liquidBotService.seedMarket(yesTicker, noTicker);

        return toDto(entity);
    }

    public MarketEvent getEvent(String id) {
        return eventRepo.findById(id).map(this::toDto).orElse(null);
    }

    public List<MarketEvent> getAllEvents() {
        return eventRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void closeEvent(String id) {
        // closed = no new orders; settled = payouts done
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
