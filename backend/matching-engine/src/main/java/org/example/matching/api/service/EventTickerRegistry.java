package org.example.matching.api.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks YES/NO ticker pairs for prediction market events.
 * Allows OrderService to convert "sell without shares" into a buy of the counterpart.
 */
@Component
public class EventTickerRegistry {

    // Each event ticker maps to its counterpart (YES → NO, NO → YES)
    private final Map<String, String> counterpartMap = new ConcurrentHashMap<>();

    public void registerPair(String yesTicker, String noTicker) {
        counterpartMap.put(yesTicker, noTicker);
        counterpartMap.put(noTicker, yesTicker);
    }

    /** Returns the counterpart ticker, or null if this is not an event ticker. */
    public String getCounterpart(String ticker) {
        return counterpartMap.get(ticker);
    }
}
