package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wallet_shares")
@IdClass(WalletShareId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletShareEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "instrument")
    private String instrument;

    @Column(name = "available_qty", nullable = false)
    private long availableQty;

    @Column(name = "reserved_qty", nullable = false)
    private long reservedQty;
}
