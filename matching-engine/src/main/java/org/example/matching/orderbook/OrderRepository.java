package org.example.matching.orderbook;


import lombok.AllArgsConstructor;
import org.example.matching.model.Order;

import java.util.Optional;


public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String orderId);
}