package org.example.matching.orderbook;

import lombok.RequiredArgsConstructor;
import org.example.matching.entity.OrderEntity;
import org.example.matching.model.Order;
import org.example.matching.model.OrderSide;
import org.example.matching.Repository.OrderJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DatabaseOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpa;

    @Override
    public void save(Order order) {
        jpa.save(OrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .instrument(order.getInstrument())
                .side(order.getSide().name())
                .price(order.getPrice())
                .originalQty(order.getQuantity())
                .remainingQty(order.getQuantity())
                .status("OPEN")
                .timestamp(order.getTimestamp())
                .build());
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return jpa.findById(orderId).map(this::toDomain);
    }

    private Order toDomain(OrderEntity e) {
        return new Order(
                e.getId(),
                e.getUserId(),
                e.getPrice(),
                e.getRemainingQty(),
                e.getTimestamp(),
                OrderSide.valueOf(e.getSide()),
                e.getInstrument()
        );
    }
}
