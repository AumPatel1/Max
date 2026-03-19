package org.example.matching.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.matching.api.service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles raw WebSocket connections for market data streaming.
 *
 * Clients connect to ws://host:8080/ws/market and exchange JSON frames:
 *   subscribe   -> { "type": "subscribe",   "market": "AAPL", "lastSeq": 1234 }
 *   unsubscribe -> { "type": "unsubscribe", "market": "AAPL" }
 *   heartbeat   -> { "type": "heartbeat" }
 *
 * Server pushes: snapshot, book_update, trade, heartbeat, error
 */
@Slf4j
@Component
public class MarketWebSocketHandler extends TextWebSocketHandler {

    // All open sessions
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // session-id  -> set of markets the session is subscribed to
    private final ConcurrentHashMap<String, Set<String>> sessionToMarkets = new ConcurrentHashMap<>();
    //client-> to markets they  wants to subs // so string [ client 1, clinet 2] - > market theya re subscirbed to are [ a] , [a,b,c] respectively

    // market-id -> set of session-ids subscribed to that market
    private final ConcurrentHashMap<String, Set<String>> marketToSessions = new ConcurrentHashMap<>();
    //market-> to who clients wants it
    // reverse , sees which clients set  are subscribed to a share e.g [appl]->[client-a,clinetb]


    // Simple rate limiting: per session [messageCount, windowStartMs]
    private final ConcurrentHashMap<String, long[]> rateLimits = new ConcurrentHashMap<>();

    private static final int  RATE_LIMIT     = 30;      // max messages per window
    private static final long RATE_WINDOW_MS = 10_000L; // 10-second rolling window

    // @Lazy breaks the circular dependency: MarketDataService <-> MarketWebSocketHandler
    @Lazy
    @Autowired
    private MarketDataService marketDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    // Connection lifecycle


    //called automatically by spring after client hits this endpoint - endpoint ws://host:8080/ws/marke
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sid = session.getId();
        sessions.put(sid, session);
        sessionToMarkets.put(sid, ConcurrentHashMap.newKeySet());
        rateLimits.put(sid, new long[]{0L, System.currentTimeMillis()});
        log.debug("WS connected: {}", sid);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sid = session.getId();
        Set<String> markets = sessionToMarkets.remove(sid);
        if (markets != null) {
            for (String market : markets) {
                Set<String> subs = marketToSessions.get(market);
                if (subs != null) subs.remove(sid);
                marketDataService.unsubscribe(sid, market);
            }
        }
        sessions.remove(sid);
        rateLimits.remove(sid);
        log.debug("WS disconnected: {} status={}", sid, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        log.warn("WS transport error session={}: {}", session.getId(), ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // Inbound message dispatch
    // -------------------------------------------------------------------------

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sid = session.getId();

        if (!checkRateLimit(sid)) {
            sendToSession(sid, errorJson("rate_limit_exceeded", "Too many requests"));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(message.getPayload());
        } catch (Exception e) {
            sendToSession(sid, errorJson("bad_request", "Invalid JSON"));
            return;
        }

        String type = root.path("type").asText("");

        switch (type) {
            case "subscribe" -> {
                String market = root.path("market").asText(null);
                if (market == null || market.isBlank()) {
                    sendToSession(sid, errorJson("bad_request", "missing 'market' field"));
                    return;
                }
                long lastSeq = root.path("lastSeq").asLong(0L);
                // Register the subscription mapping first so snapshot/events reach this session
                addSubscription(sid, market);
                // Delegate snapshot-or-replay logic to MarketDataService
                marketDataService.subscribe(sid, market, lastSeq, session);
            }
            case "unsubscribe" -> {
                String market = root.path("market").asText(null);
                if (market != null && !market.isBlank()) {
                    removeSubscription(sid, market);
                    marketDataService.unsubscribe(sid, market);
                }
            }
            case "heartbeat" -> {
                // Echo heartbeat back
                String hb = objectMapper.writeValueAsString(
                        Map.of("type", "heartbeat", "timestamp", System.currentTimeMillis()));
                sendToSession(sid, hb);
            }
            default -> sendToSession(sid, errorJson("bad_request", "unknown type: " + type));
        }
    }

    // -------------------------------------------------------------------------
    // Outbound helpers (called by MarketDataService)
    // -------------------------------------------------------------------------

    /** Send a JSON string to a single session (thread-safe). */
    public void sendToSession(String sessionId, String json) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {          // WebSocketSession is not thread-safe
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                log.warn("Failed to send to {}: {}", sessionId, e.getMessage());
            }
        }
    }

    /** Broadcast a JSON string to all sessions subscribed to a market. */
    public void sendToMarket(String marketId, String json) {
        Set<String> sids = marketToSessions.getOrDefault(marketId, Collections.emptySet());
        for (String sid : sids) {
            sendToSession(sid, json);
        }
    }

    // -------------------------------------------------------------------------
    // Subscription bookkeeping
    // -------------------------------------------------------------------------

    void addSubscription(String sessionId, String marketId) {
        sessionToMarkets.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(marketId);
        marketToSessions.computeIfAbsent(marketId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    void removeSubscription(String sessionId, String marketId) {
        Set<String> markets = sessionToMarkets.get(sessionId);
        if (markets != null) markets.remove(marketId);
        Set<String> sids = marketToSessions.get(marketId);
        if (sids != null) sids.remove(sessionId);
    }

    // -------------------------------------------------------------------------
    // Rate limiting
    // -------------------------------------------------------------------------

    private boolean checkRateLimit(String sessionId) {
        long[] rl = rateLimits.get(sessionId);
        if (rl == null) return true;
        long now = System.currentTimeMillis();
        if (now - rl[1] > RATE_WINDOW_MS) {
            rl[0] = 1L;
            rl[1] = now;
            return true;
        }
        return ++rl[0] <= RATE_LIMIT;
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private String errorJson(String code, String message) {
        try {
            return objectMapper.writeValueAsString(
                    Map.of("type", "error", "code", code, "message", message));
        } catch (Exception e) {
            return "{\"type\":\"error\"}";
        }
    }
}
