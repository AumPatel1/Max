package org.example.matching.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Trade {
    private String buyOrderId;
    private String sellOrderId;
    private long price;
    private long quantity;
    private long timestamp;

    public String getBuyOrderId() {
        return buyOrderId;
    }

    public String getSellOrderId() {
        return sellOrderId;
    }

    public long getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }
}