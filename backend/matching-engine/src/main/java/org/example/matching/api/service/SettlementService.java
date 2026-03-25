package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.EventStatus;
import org.example.matching.api.dto.MarketEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final MarketManagmentService marketManagmentService;
    private final WalletService walletService;

    /**
     * Settles a prediction market event.
     * outcome must be "YES" or "NO".
     * Winners (holders of the winning ticker) receive full payout.
     */
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

        marketManagmentService.closeEvent(eventId);
        event.setStatus(EventStatus.SETTLED);

        // Determine winning and losing tickers
        String winningTicker = outcome.equalsIgnoreCase("YES") ? event.getYesTicker() : event.getNoTicker();
        String losingTicker  = outcome.equalsIgnoreCase("YES") ? event.getNoTicker()  : event.getYesTicker();

        // Release reserved cash/shares for all open (unfilled) orders on both tickers
        walletService.releaseAllReservationsForInstrument(winningTicker);
        walletService.releaseAllReservationsForInstrument(losingTicker);

        // Credit holders of the winning ticker 100 cash per share (YES+NO = 100 in this system)
        for (org.example.matching.model.Wallet wallet : walletService.getAllWallets()) {
            long winningAvail    = wallet.getAvailableShares(winningTicker);
            long winningReserved = wallet.getReservedShares(winningTicker);
            long totalWinning    = winningAvail + winningReserved;

            if (totalWinning > 0) {
                walletService.creditUserCash(wallet.getUserId(), totalWinning * 100L);
            }

            // Zero out all share balances for both tickers
            zeroClearShares(wallet, winningTicker);
            zeroClearShares(wallet, losingTicker);
        }
    }

    private void zeroClearShares(org.example.matching.model.Wallet wallet, String ticker) {
        long avail    = wallet.getAvailableShares(ticker);
        long reserved = wallet.getReservedShares(ticker);
        if (avail    > 0) wallet.getAvailableShares().computeIfAbsent(ticker, k -> new java.util.concurrent.atomic.AtomicLong(0)).addAndGet(-avail);
        if (reserved > 0) wallet.getReservedShares().computeIfAbsent(ticker,  k -> new java.util.concurrent.atomic.AtomicLong(0)).addAndGet(-reserved);
    }
}
