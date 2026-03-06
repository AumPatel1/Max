package org.example.matching.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {
    private String userId;
    private long amount;
    private String instrument; // Optional: used if depositing shares instead of cash

    public String getUserId() {
        return userId;
    }

    public long getAmount() {
        return amount;
    }

    public String getInstrument() {
        return instrument;
    }
}
