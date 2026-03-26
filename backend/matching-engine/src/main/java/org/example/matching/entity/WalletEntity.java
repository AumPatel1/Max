package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WalletEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "available_cash", nullable = false)
    private long availableCash;

    @Column(name = "reserved_cash", nullable = false)
    private long reservedCash;
}

/**
 *
 * @Entity - Table name (wallet)
 * contr and getsets
 * class walletentity
 * private String userId,
 * private long avaiablecash,
 * priavte long reservedcash
 */