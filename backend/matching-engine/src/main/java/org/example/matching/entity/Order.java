package org.example.matching.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @Enumerated(EnumType.STRING)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(precision = 5, scale = 2)
    private BigDecimal price;

    private Integer quantity;

    private Integer filledQuantity = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum OrderSide {
        YES, NO
    }

    public enum OrderType {
        MARKET, LIMIT
    }

    public enum OrderStatus {
        PENDING, FILLED, PARTIAL, CANCELLED
    }
}
