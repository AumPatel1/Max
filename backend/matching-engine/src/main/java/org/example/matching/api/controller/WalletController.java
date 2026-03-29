package org.example.matching.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.DepositRequest;
import org.example.matching.api.dto.WalletResponse;
import org.example.matching.model.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

// Controllers face to implementations and they were never connected to any service class a hardcore -
// they were connected by  pre- service e.g. public interface WalletService {
// that is an implementation , before it used be for InMemoryWalletService , now we have class DatabaseWalletService
// with @Service annotation so now it will map all req there , controller only said give me service and not any particular service
// that Spring does on its own
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * GET /api/wallets/me
     * Returns the authenticated user's own wallet.
     * userId is extracted from the JWT — the client never sends it.
     *
     * SecurityContextHolder.getContext().getAuthentication().getName()
     *   → returns the "subject" claim from the JWT (= the username set during login/register)
     *   → this is the same value used as the walletId when the wallet was created on registration
     */
    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet() {
        // Extract userId from the JWT via SecurityContextHolder — never trust user-supplied userId
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Wallet wallet = walletService.getWallet(userId);
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }

        // Convert AtomicLong maps to Long maps for JSON response
        Map<String, Long> availableShares = wallet.getAvailableShares().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get()
                ));

        Map<String, Long> reservedShares = wallet.getReservedShares().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get()
                ));

        WalletResponse response = WalletResponse.builder()
                .userId(userId)
                .availableCash(wallet.getAvailableCash())
                .reservedCash(wallet.getReservedCash())
                .availableShares(availableShares)
                .reservedShares(reservedShares)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/wallets/depositCash
     * Credits cash to the authenticated user's wallet.
     * userId is extracted from the JWT — the request body only carries the amount.
     */
    @PostMapping("/depositCash")
    public ResponseEntity<String> depositCash(@RequestBody DepositRequest request) {
        // Extract userId from JWT — prevents users from depositing cash into someone else's wallet
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        walletService.creditUserCash(userId, request.getAmount());
        return ResponseEntity.ok("Deposit Successful");
    }

    /**
     * POST /api/wallets/depositShares
     * Credits shares for a given instrument to the authenticated user's wallet.
     * userId is extracted from the JWT — the request body only carries amount + instrument.
     */
    @PostMapping("/depositShares")
    public ResponseEntity<String> depositShares(@RequestBody DepositRequest request) {
        // Extract userId from JWT — same security reason as depositCash
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        // Credit shares for the specific instrument
        walletService.creditUserShares(userId, request.getInstrument(), request.getAmount());
        return ResponseEntity.ok("Shares Deposited Successfully");
    }
}
