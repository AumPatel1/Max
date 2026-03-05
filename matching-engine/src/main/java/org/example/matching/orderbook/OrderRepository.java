package org.example.matching.orderbook;


import lombok.AllArgsConstructor;
import org.example.matching.model.Order;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//@Repository
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String orderId);
}
