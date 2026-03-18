package org.example.matching.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.matching.api.dto.OrderBookResponse;
import org.example.matching.api.dto.PriceLevel;
import org.example.matching.model.Trade;
import org.example.matching.ws.MarketWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class MarketDataService {

    // =========================================================================
    // EXISTING fields – untouched so REST endpoints keep working
    // =========================================================================

    private final Map<String, Double> lastPrices   = new ConcurrentHashMap<>();
    private final Map<String, Double> sumPV         = new ConcurrentHashMap<>();
    private final Map<String, Long>   sumV          = new ConcurrentHashMap<>();
    private final Map<String, Double> bestBids      = new ConcurrentHashMap<>();
    private final Map<String, Double> bestAsks      = new ConcurrentHashMap<>();
    private final Map<String, List<Double>> priceHistory = new ConcurrentHashMap<>();

    // =========================================================================
    // NEW fields for WebSocket streaming
    // =========================================================================

    // Monotonically increasing sequence number per market
    private final Map<String, AtomicLong> seqNumbers = new ConcurrentHashMap<>();

    // Ring buffer of the last MAX_BUFFER events per market (for reconnect replay)
    private final Map<String, ArrayDeque<BufferedEvent>> eventBuffers = new ConcurrentHashMap<>();
    private static final int MAX_BUFFER     = 10_000;
    private static final int MAX_REPLAY_GAP = 10_000;

    // EMA (20-period) per market
    private final Map<String, Double> emaValues = new ConcurrentHashMap<>();
    private static final double EMA_ALPHA = 2.0 / 21.0; // 2 / (period + 1)

    // Previous book levels – needed to compute incremental diffs
    private final Map<String, List<PriceLevel>> prevBids = new ConcurrentHashMap<>();
    private final Map<String, List<PriceLevel>> prevAsks = new ConcurrentHashMap<>();

    // Per-market lock to keep seq + buffer updates atomic across threads
    private final Map<String, Object> marketLocks = new ConcurrentHashMap<>();

    // @Lazy breaks MarketDataService <-> MarketWebSocketHandler circular dep
    @Lazy
    @Autowired
    private MarketWebSocketHandler wsHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================================
    // EXISTING public methods – signatures unchanged
    // =========================================================================

    public void UpdateTrade(String instrument, long price, long quantity) {
        lastPrices.put(instrument, (double) price);
        sumPV.merge(instrument, (double) (price * quantity), Double::sum);
        sumV.merge(instrument, quantity, Long::sum);

        List<Double> history = priceHistory.computeIfAbsent(instrument, k -> new CopyOnWriteArrayList<>());
        history.add(0, (double) price);
        if (history.size() > 10) history.remove(history.size() - 1);
    }

    public void updateBookTops(String instrument, Double bestBid, Double bestAsk) {
        if (bestBid != null) bestBids.put(instrument, bestBid);
        if (bestAsk != null) bestAsks.put(instrument, bestAsk);
    }

    public Map<String, Object> getSnapshots(String instrument) {
        double ltp  = lastPrices.getOrDefault(instrument, 0.0);
        double vwap = sumV.getOrDefault(instrument, 0L) == 0
                ? 0 : sumPV.get(instrument) / sumV.get(instrument);
        double bid  = bestBids.getOrDefault(instrument, 0.0);
        double ask  = bestAsks.getOrDefault(instrument, 0.0);
        double mid  = (bid > 0 && ask > 0) ? (bid + ask) / 2.0 : ltp;

        return Map.of(
                "instrument", instrument,
                "lastPrice",  ltp,
                "vwap",       vwap,
                "bid",        bid,
                "ask",        ask,
                "mid",        mid,
                "spread",     (ask - bid)
        );
    }

    public List<Double> getPriceHistory(String instrument) {
        return priceHistory.getOrDefault(instrument, List.of());
    }

    // =========================================================================
    // NEW public methods – called by OrderService after each trade / book change
    // =========================================================================

    /**
     * Called after every matched trade.
     * Updates existing stats AND pushes a trade event to subscribed WS clients.
     */
    public void onTrade(String instrument, Trade trade) {
        // Keep existing REST snapshot data up to date
        UpdateTrade(instrument, trade.getPrice(), trade.getQuantity());

        // Build + broadcast WS trade event (seq-stamped, buffered for replay)
//getMarketLock is just a map
        Object lock = getMarketLock(instrument);
        String json;
        //synchronized(lock){
       //String json;
        synchronized (lock) {
            // nextSeq is also a map
            long seq = nextSeq(instrument);
            updateEma(instrument, trade.getPrice());
            json = buildTradeJson(instrument, seq, trade);
            bufferEvent(instrument, seq, json);
        }
        wsHandler.sendToMarket(instrument, json);
    }
    /**
     * long seq = nextSeq(instrument)
     */

    /**
     * Called once per order placement (after all trades are settled).
     * Diffs the current book top against the previous snapshot and pushes
     * a book_update event carrying only the changed / removed levels.
     */
    public void onBookChange(String instrument, OrderBookResponse snapshot) {
        // Keep existing REST top-of-book data up to date
        List<PriceLevel> bids = limitLevels(snapshot.getBids(), 20);
        List<PriceLevel> asks = limitLevels(snapshot.getAsks(), 20);

        updateBookTops(instrument,
                bids.isEmpty() ? 0.0 : bids.get(0).getPrice().doubleValue(),
                asks.isEmpty() ? 0.0 : asks.get(0).getPrice().doubleValue());

        // Compute incremental diff against previous state
        List<Map<String, Object>> changes = new ArrayList<>();
        changes.addAll(computeChanges(
                prevBids.getOrDefault(instrument, Collections.emptyList()), bids, "bid"));
        changes.addAll(computeChanges(
                prevAsks.getOrDefault(instrument, Collections.emptyList()), asks, "ask"));

        // Save current levels as "previous" for next diff
        prevBids.put(instrument, bids);
        prevAsks.put(instrument, asks);

        if (changes.isEmpty()) return; // nothing actually changed → no event

        Object lock = getMarketLock(instrument);
        String json;
        synchronized (lock) {
            long seq = nextSeq(instrument);
            json = buildBookUpdateJson(instrument, seq, changes);
            bufferEvent(instrument, seq, json);
        }
        wsHandler.sendToMarket(instrument, json);
    }

    /**
     * Called from MarketWebSocketHandler when a client subscribes.
     * Sends missed events if the gap is small, otherwise a fresh snapshot.
     */
    public void subscribe(String sessionId, String marketId, long lastSeq, WebSocketSession session) {
        long currentSeq = seqNumbers.getOrDefault(marketId, new AtomicLong(0)).get();

        if (lastSeq > 0 && currentSeq - lastSeq <= MAX_REPLAY_GAP && currentSeq > lastSeq) {
            // Client reconnected with a known seq – replay missed events
            sendMissedEvents(sessionId, marketId, lastSeq);
        } else {
            // Fresh subscribe or gap too large – send full snapshot
            // We need the current book from the MatchingEngine; we use prevBids/prevAsks
            // which were populated on the last onBookChange call.
            sendSnapshot(sessionId, marketId);
        }
    }

    /** Called from the handler on unsubscribe or disconnect (no-op here; handler owns session maps). */
    public void unsubscribe(String sessionId, String marketId) {
        // Session cleanup lives in MarketWebSocketHandler.
        // Add per-session state teardown here if needed in the future.
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private void sendSnapshot(String sessionId, String marketId) {
        //get the market
        long seq = seqNumbers.getOrDefault(marketId, new AtomicLong(0)).get();
        List<PriceLevel> bids = prevBids.getOrDefault(marketId, Collections.emptyList());
        List<PriceLevel> asks = prevAsks.getOrDefault(marketId, Collections.emptyList());
        String json = buildSnapshotJson(marketId, seq, bids, asks);
        wsHandler.sendToSession(sessionId, json);
    }

    private void sendMissedEvents(String sessionId, String marketId, long lastSeq) {
        ArrayDeque<BufferedEvent> buffer = eventBuffers.getOrDefault(marketId, new ArrayDeque<>());
        List<BufferedEvent> missed = new ArrayList<>();
        synchronized (getMarketLock(marketId)) {
            for (BufferedEvent e : buffer) {
                if (e.seq() > lastSeq) missed.add(e);
            }
        }
        for (BufferedEvent e : missed) {
            wsHandler.sendToSession(sessionId, e.json());
        }
    }

    private long nextSeq(String market) {
        return seqNumbers.computeIfAbsent(market, k -> new AtomicLong(0)).incrementAndGet();
    }

    private void bufferEvent(String market, long seq, String json) {
        ArrayDeque<BufferedEvent> buf =
                eventBuffers.computeIfAbsent(market, k -> new ArrayDeque<>());
        buf.addLast(new BufferedEvent(seq, json));
        if (buf.size() > MAX_BUFFER) buf.removeFirst();
    }

    private void updateEma(String instrument, long price) {
        double prev = emaValues.getOrDefault(instrument, (double) price);
        emaValues.put(instrument, EMA_ALPHA * price + (1 - EMA_ALPHA) * prev);
    }

    private Object getMarketLock(String market) {
        return marketLocks.computeIfAbsent(market, k -> new Object());
    }

    // -------------------------------------------------------------------------
    // JSON builders  (use plain Map → Jackson, keeps it dependency-light)
    // -------------------------------------------------------------------------

    private String buildTradeJson(String market, long seq, Trade trade) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type",        "trade");
        m.put("market",      market);
        m.put("seq",         seq);
        m.put("timestamp",   trade.getTimestamp());
        m.put("price",       trade.getPrice());
        m.put("quantity",    trade.getQuantity());
        m.put("buyOrderId",  trade.getBuyOrderId());
        m.put("sellOrderId", trade.getSellOrderId());
        return toJson(m);
    }

    private String buildBookUpdateJson(String market, long seq, List<Map<String, Object>> changes) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type",      "book_update");
        m.put("market",    market);
        m.put("seq",       seq);
        m.put("timestamp", System.currentTimeMillis());
        m.put("changes",   changes);
        return toJson(m);
    }

    private String buildSnapshotJson(String market, long seq,
                                     List<PriceLevel> bids, List<PriceLevel> asks) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type",      "snapshot");
        m.put("market",    market);
        m.put("seq",       seq);
        m.put("timestamp", System.currentTimeMillis());
        m.put("bestBid",   bids.isEmpty() ? 0 : bids.get(0).getPrice());
        m.put("bestAsk",   asks.isEmpty() ? 0 : asks.get(0).getPrice());
        m.put("bids",      priceLevelsToMaps(bids));
        m.put("asks",      priceLevelsToMaps(asks));

        double vwap = sumV.getOrDefault(market, 0L) == 0
                ? 0 : sumPV.get(market) / sumV.get(market);
        m.put("vwap",     vwap);
        m.put("ema_mark", emaValues.getOrDefault(market, 0.0));
        return toJson(m);
    }

    // -------------------------------------------------------------------------
    // Diff computation
    // -------------------------------------------------------------------------

    /**
     * Returns only the levels that changed (qty differs) or were removed (qty=0)
     * compared to the previous snapshot for that side.
     */
    private List<Map<String, Object>> computeChanges(List<PriceLevel> prev,
                                                      List<PriceLevel> curr,
                                                      String side) {
        Map<Long, Long> prevMap = new LinkedHashMap<>();
        for (PriceLevel pl : prev) prevMap.put(pl.getPrice(), pl.getQuantity());

        Map<Long, Long> currMap = new LinkedHashMap<>();
        for (PriceLevel pl : curr) currMap.put(pl.getPrice(), pl.getQuantity());

        List<Map<String, Object>> changes = new ArrayList<>();

        // Added or quantity-changed levels
        for (Map.Entry<Long, Long> e : currMap.entrySet()) {
            if (!e.getValue().equals(prevMap.get(e.getKey()))) {
                changes.add(Map.of("side", side, "price", e.getKey(), "qty", e.getValue()));
            }
        }
        // Removed levels (no longer in book → qty = 0)
        for (Long price : prevMap.keySet()) {
            if (!currMap.containsKey(price)) {
                changes.add(Map.of("side", side, "price", price, "qty", 0L));
            }
        }
        return changes;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private List<PriceLevel> limitLevels(List<PriceLevel> levels, int max) {
        if (levels == null) return Collections.emptyList();
        return levels.size() <= max ? levels : levels.subList(0, max);
    }

    private List<Map<String, Object>> priceLevelsToMaps(List<PriceLevel> levels) {
        List<Map<String, Object>> result = new ArrayList<>(levels.size());
        for (PriceLevel pl : levels) {
            result.add(Map.of("price", pl.getPrice(), "qty", pl.getQuantity()));
        }
        return result;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON serialization failed", e);
            return "{}";
        }
    }

    // -------------------------------------------------------------------------
    // Inner record – one buffered event for replay
    // -------------------------------------------------------------------------

    private record BufferedEvent(long seq, String json) {}
}
