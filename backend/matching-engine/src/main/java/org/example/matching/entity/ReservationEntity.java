package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationEntity {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "instrument", nullable = false)
    private String instrument;

    @Column(name = "is_buy", nullable = false)
    private boolean isBuy;

    @Column(name = "price_at_reserve", nullable = false)
    private long priceAtReserve;

    @Column(name = "remaining_qty", nullable = false)
    private int remainingQty;

    @Column(name = "reserved_cash", nullable = false)
    private long reservedCash;

    @Column(name = "reserved_shares", nullable = false)
    private long reservedShares;
}
