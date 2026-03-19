package org.example.matching.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String status;      // "ACCEPTED", "REJECTED"
    private String message;     // "Insufficient funds" or "Success"
    private long timestamp;

    public String getStatus() {
        return status;
    }
}