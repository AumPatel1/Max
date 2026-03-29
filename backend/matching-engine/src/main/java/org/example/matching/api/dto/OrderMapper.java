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
        // Normalize to uppercase so all internal keys (orderBooks map, market data maps,
        // wallet share maps) are consistent regardless of how the client submitted the ticker.
        order.setInstrument(request.getInstrument().toUpperCase());
        return order;
    }
}