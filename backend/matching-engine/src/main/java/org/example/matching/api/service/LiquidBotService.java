package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.api.dto.OrderRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LiquidBotService {
    private final OrderService orderService;

    public void seedMarket(String yesTicker, String noTicker) {
        // Providing liquidity for YES
        placeBotOrder(yesTicker, "BUY", 45, 1000);   // Bot buys from users at 45c
        placeBotOrder(yesTicker, "SELL", 55, 1000);  // Bot sells to users at 55c

        // Providing liquidity for NO
        placeBotOrder(noTicker, "BUY", 45, 1000);    // Bot buys from users at 45c
        placeBotOrder(noTicker, "SELL", 55, 1000);   // Bot sells to users at 55c
    }

    private void placeBotOrder(String ticker, String side, long price, long qty) {
        OrderRequest req = new OrderRequest();
        req.setUserId("HOUSE_BOT");
        req.setInstrument(ticker);
        req.setSide(side);
        req.setPrice(price);
        req.setQuantity(qty);
        req.setIdempotencyKey(UUID.randomUUID().toString());
        orderService.processOrder(req);
    }
}
