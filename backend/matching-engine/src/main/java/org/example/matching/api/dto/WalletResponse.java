package org.example.matching.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private String userId;
    private long availableCash;
    private long reservedCash;
    private Map<String, Long> availableShares;
    private Map<String, Long> reservedShares;
}