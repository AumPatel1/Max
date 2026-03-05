package org.example.matching.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private String orderId;
    private String status;      // "ACCEPTED", "REJECTED"
    private String message;     // "Insufficient funds" or "Success"
    private long timestamp;
}