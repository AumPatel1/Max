package org.example.matching.repository;

import org.example.matching.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByMarketIdAndStatus(Long marketId, Order.OrderStatus status);
    List<Order> findByMarketIdAndSideAndStatus(Long marketId, Order.OrderSide side, Order.OrderStatus status);
}
