package org.example.matching.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class Reservation {
    private final String OrderId;
    private final String userId;
    private final long priceatReserve;
    private int remainingQty;
    private String instrument; // Add instrument field

    private long reservedCash;
    private long reservedShares;
    private final Boolean isBuy;

    public Reservation(String orderId, String userId, long priceatReserve, int remainingQty, Boolean isBuy) {
        OrderId = orderId;
        this.userId = userId;
        this.priceatReserve = priceatReserve;
        this.remainingQty = remainingQty;
        this.isBuy = isBuy;
        this.instrument = "MARKET"; // Default instrument
    }

    public Reservation(String orderId, String userId, long priceatReserve, int remainingQty, Boolean isBuy, String instrument) {
        OrderId = orderId;
        this.userId = userId;
        this.priceatReserve = priceatReserve;
        this.remainingQty = remainingQty;
        this.isBuy = isBuy;
        this.instrument = instrument != null ? instrument : "MARKET";
    }

    public long reduceBy(int qty) {
        if (qty <= 0 || remainingQty <= 0) {
            return 0;
        }

        // Don't consume more than what is left in this specific reservation
        int consumed = Math.min(qty, remainingQty);
        long cashUsed =0;
        if (isBuy) {
            // Calculate how much cash was 'used' for this portion of the trade
            cashUsed = (long) consumed * priceatReserve;

            // Subtract the used cash from the total reserved cash
            this.reservedCash -= cashUsed;

            if (this.reservedCash < 0) this.reservedCash = 0;
        } else {
            // For a sell order, we just reduce the count of shares held
            this.reservedShares -= consumed;

            if (this.reservedShares < 0) this.reservedShares = 0;
        }

        // Finally, reduce the quantity so we know how much of the order is left to fill
        this.remainingQty -= consumed;
        return  cashUsed;
    }

    // Additional getters needed by the codebase
    public String getOrderId() {
        return OrderId;
    }

    public String getUserId() {
        return userId;
    }

    public Boolean getIsBuy() {
        return isBuy;
    }

    public long getReservedCash() {
        return reservedCash;
    }

    public long getReservedShares() {
        return reservedShares;
    }

    public int getRemainingQty() {
        return remainingQty;
    }

    public void setReservedCash(long reservedCash) {
        this.reservedCash = reservedCash;
    }

    public void setReservedShares(long reservedShares) {
        this.reservedShares = reservedShares;
    }
}