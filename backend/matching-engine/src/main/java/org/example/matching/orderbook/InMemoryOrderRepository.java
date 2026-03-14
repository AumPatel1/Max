package org.example.matching.orderbook;

import org.example.matching.model.Order;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String,Order> map = new ConcurrentHashMap<>();

    @Override
    public void save(Order order){
        map.put(order.getId(),order);
    }
    @Override
    public Optional<Order> findById(String OrderId){
        return Optional.ofNullable(map.get(OrderId));
    }

}
