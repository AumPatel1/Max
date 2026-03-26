package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.EventStatus;
import org.example.matching.api.dto.MarketEvent;
import org.example.matching.model.Wallet;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final MarketManagmentService marketManagmentService;
    private final WalletService walletService;

    public void settleEvent(String eventId, String outcome) {
        MarketEvent event = marketManagmentService.getEvent(eventId);
        if (event == null) {
            throw new IllegalStateException("Event not found: " + eventId);
        }
        if (event.getStatus() == EventStatus.SETTLED) {
            throw new IllegalStateException("Event already settled: " + eventId);
        }
        if (!outcome.equalsIgnoreCase("YES") && !outcome.equalsIgnoreCase("NO")) {
            throw new IllegalArgumentException("Outcome must be YES or NO, got: " + outcome);
        }

        String winningTicker = outcome.equalsIgnoreCase("YES") ? event.getYesTicker() : event.getNoTicker();
        String losingTicker  = outcome.equalsIgnoreCase("YES") ? event.getNoTicker()  : event.getYesTicker();

        // Credit every wallet that holds winning shares
        for (Wallet wallet : walletService.getAllWallets()) {
            long totalWinning = wallet.getAvailableShares(winningTicker)
                              + wallet.getReservedShares(winningTicker);
            if (totalWinning > 0) {
                walletService.creditUserCash(wallet.getUserId(), totalWinning * 100L);
            }
            // Zero out both tickers via the service (works for both DB and in-memory)
            walletService.zeroOutShares(wallet.getUserId(), winningTicker);
            walletService.zeroOutShares(wallet.getUserId(), losingTicker);
        }

        marketManagmentService.settleEvent(eventId);
    }
}
