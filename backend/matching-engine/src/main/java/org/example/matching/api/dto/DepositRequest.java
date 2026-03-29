package org.example.matching.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for deposit endpoints.
 *
 * userId is intentionally absent — it is extracted from the JWT by WalletController
 * and never trusted from the request body. This prevents a user from depositing
 * funds into another user's wallet.
 *
 *   POST /api/wallets/depositCash    →  requires: amount
 *   POST /api/wallets/depositShares  →  requires: amount + instrument
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {
    private long amount;
    private String instrument; // Optional: used if depositing shares instead of cash

    public long getAmount() {
        return amount;
    }

    public String getInstrument() {
        return instrument;
    }
}
