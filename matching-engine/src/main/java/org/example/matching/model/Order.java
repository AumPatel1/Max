package org.example.matching.model;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Order {
    String id;
    String userId;
    long price;
    int quantity;
    long timestamp;
    OrderSide side;
    String instrument; // Added for multi-book support

    public Order(String userId, long price, int quantity, long timestamp, OrderSide side) {
        this.userId = userId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.side = side;
        this.instrument = "DEFAULT"; // Default instrument
    }

    public Order(String id, String userId, long price, int quantity, long timestamp, OrderSide side) {
        this.id = id;
        this.userId = userId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.side = side;
        this.instrument = "DEFAULT"; // Default instrument
    }

    public Order(String id, String userId, long price, int quantity, long timestamp, OrderSide side, String instrument) {
        this.id = id;
        this.userId = userId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.side = side;
        this.instrument = instrument != null ? instrument : "DEFAULT";
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public long getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public OrderSide getSide() {
        return side;
    }
    public void reduceQuantity(long delta) {
        if (delta < 0) throw new IllegalArgumentException("delta must be >= 0");
        if (delta > quantity) throw new IllegalArgumentException("reduce more than remaining");
        this.quantity -= delta;
    }
    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                ", side=" + side +
                '}';
    }
    
    public String getInstrument() {
        return instrument;
    }
    
    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }
}
