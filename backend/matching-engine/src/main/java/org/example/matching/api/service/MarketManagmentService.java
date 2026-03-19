package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.EventStatus;
import org.example.matching.api.dto.MarketEvent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class MarketManagmentService {

    private final LiquidBotService liquidBotService;
    private final WalletService walletService;
    private final EventTickerRegistry eventTickerRegistry;

    private final Map<String, MarketEvent> events = new ConcurrentHashMap<>();

    /**
     * Creates a new prediction market event.
     * Seeds HOUSE_BOT with funds and initial liquidity on both YES/NO books.
     */
    public MarketEvent createEvent(String id, String question, String yesTicker, String noTicker, int expiry) {
        if (events.containsKey(id)) {
            return events.get(id);
        }

        // Give HOUSE_BOT enough funds to provide liquidity on both sides:
        // BUY at 45 x 1000 = 45,000 cash per ticker (2 tickers = 90,000 total)
        // SELL at 55 x 1000 = 1000 shares per ticker
        walletService.creditUserCash("HOUSE_BOT", 100_000L);
        walletService.creditUserShares("HOUSE_BOT", yesTicker, 2000L);
        walletService.creditUserShares("HOUSE_BOT", noTicker, 2000L);

        MarketEvent event = MarketEvent.builder()
                .id(id)
                .question(question)
                .yesTicker(yesTicker)
                .noTicker(noTicker)
                .expiry(expiry)
                .status(EventStatus.OPEN)
                .build();

        events.put(id, event);

        // Register YES/NO pair so OrderService can convert sells-without-shares to buys
        eventTickerRegistry.registerPair(yesTicker, noTicker);

        // Seed initial order book liquidity so traders always have a price to trade against
        liquidBotService.seedMarket(yesTicker, noTicker);

        return event;
    }
//    private long getNextSeq(String ticker) {
//        return sequences.computeIfAbsent(ticker, k -> new AtomicLong(0)).incrementAndGet();
//    }

    public MarketEvent getEvent(String id) {
        return events.get(id);
    }

    public void closeEvent(String id) {
        MarketEvent event = events.get(id);
        if (event != null) {
            event.setStatus(EventStatus.CLOSED);
        }
    }
}
