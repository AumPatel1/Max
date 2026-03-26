package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "market_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketEventEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "question", nullable = false, length = 1000)
    private String question;

    @Column(name = "yes_ticker", nullable = false)
    private String yesTicker;

    @Column(name = "no_ticker", nullable = false)
    private String noTicker;

    @Column(name = "expiry", nullable = false)
    private long expiry;

    @Column(name = "status", nullable = false)
    private String status;         // "OPEN", "CLOSED", "SETTLED"
}
