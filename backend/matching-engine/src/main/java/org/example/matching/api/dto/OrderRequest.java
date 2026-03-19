package org.example.matching.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
public class OrderRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Instrument is required")
    private String instrument;

    @Pattern(regexp = "BUY|SELL", message = "Side must be BUY or SELL")
    private String side;

    @Positive(message = "Price must be greater than zero")
    private long price;

    @Positive(message = "Quantity must be greater than zero")
    private long quantity;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey; // Unique string from the frontend

    public String getUserId() {
        return userId;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getSide() {
        return side;
    }

    public long getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}