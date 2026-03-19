package org.example.matching.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.matching.Wallets.WalletService;
import org.example.matching.api.dto.DepositRequest;
import org.example.matching.api.dto.WalletResponse;
import org.example.matching.model.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String userId){
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
    @PostMapping("/depositCash")
    public ResponseEntity<String> depositCash(@RequestBody DepositRequest request) {
        walletService.creditUserCash(request.getUserId(), request.getAmount());
        return ResponseEntity.ok("Deposit Successful");
    }
    
    @PostMapping("/depositShares")
    public ResponseEntity<String> depositShares(@RequestBody DepositRequest request) {
        // Credit shares for the specific instrument
        walletService.creditUserShares(request.getUserId(), request.getInstrument(), request.getAmount());
        return ResponseEntity.ok("Shares Deposited Successfully");
    }
}