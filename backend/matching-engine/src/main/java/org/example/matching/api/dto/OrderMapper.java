package org.example.matching.api.dto;

import org.example.matching.model.Order;
import org.example.matching.model.OrderSide;

import java.util.UUID;

public class OrderMapper {
    public static Order toDomain(OrderRequest request) {
        Order order = new Order(
                UUID.randomUUID().toString(),
                request.getUserId(),
                request.getPrice(),
                (int) request.getQuantity(),
                System.currentTimeMillis(),
                OrderSide.valueOf(request.getSide().toUpperCase())
        );
        order.setInstrument(request.getInstrument());
        return order;
    }
}