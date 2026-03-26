package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TradeEntity {

    @Id
    @Column(name = "id")
    private String id;             // generated UUID

    @Column(name = "buy_order_id", nullable = false)
    private String buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private String sellOrderId;

    @Column(name = "instrument", nullable = false)
    private String instrument;

    @Column(name = "price", nullable = false)
    private long price;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;
}
