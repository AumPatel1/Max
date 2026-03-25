package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buy_order_id")
    private Order buyOrder;

    @ManyToOne
    @JoinColumn(name = "sell_order_id")
    private Order sellOrder;

    @ManyToOne
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @Column(precision = 5, scale = 2)
    private BigDecimal price;

    private Integer quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalValue;

    @CreationTimestamp
    private LocalDateTime executedAt;

    // Helper methods for compatibility with existing code
    public String getBuyOrderId() {
        return buyOrder != null ? buyOrder.getId().toString() : null;
    }

    public String getSellOrderId() {
        return sellOrder != null ? sellOrder.getId().toString() : null;
    }

    public String getInstrument() {
        return market != null ? market.getQuestion() : null;
    }
}
