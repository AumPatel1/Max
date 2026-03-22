package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "markets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Market {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private String question;

    @Enumerated(EnumType.STRING)
    private MarketResult result;

    @Enumerated(EnumType.STRING)
    private MarketStatus status;

    @Column(precision = 5, scale = 2)
    private BigDecimal yesPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal noPrice;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum MarketResult {
        YES, NO, VOID
    }

    public enum MarketStatus {
        OPEN, CLOSED, RESOLVED
    }
}
