package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "instrument", nullable = false)
    private String instrument;

    @Column(name = "side", nullable = false)
    private String side;           // "BUY" or "SELL"

    @Column(name = "price", nullable = false)
    private long price;

    @Column(name = "original_qty", nullable = false)
    private int originalQty;

    @Column(name = "remaining_qty", nullable = false)
    private int remainingQty;

    @Column(name = "status", nullable = false)
    private String status;         // "OPEN", "PARTIALLY_FILLED", "FILLED", "CANCELLED"

    @Column(name = "timestamp", nullable = false)
    private long timestamp;
}

/**
 *
 * @Entity - Table name (order)
 * contr and getsets
 * class Orders
 * private String userId,
 * private String side,
 * private long price,
 *
 */
