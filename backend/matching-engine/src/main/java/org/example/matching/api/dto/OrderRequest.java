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
    // userId is NOT accepted from the client — it is injected by OrderController from the JWT.
    // No @NotBlank here: validation would reject the request before the controller sets this field.
    private String userId;

    @NotBlank(message = "Instrument is required")
    private String instrument;

    @Pattern(regexp = "BUY|SELL", message = "Side must be BUY or SELL")
    private String side;

    @Positive(message = "Price must be greater than zero")
    private long price;

    @Positive(message = "Quantity must be greater than zero")
    private long quantity;

    // Optional: if provided, the server returns the cached response for duplicate requests.
    // Not required — clients that don't need idempotency can omit it.
    private String idempotencyKey;

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