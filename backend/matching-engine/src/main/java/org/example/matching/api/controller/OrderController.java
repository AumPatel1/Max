package org.example.matching.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matching.api.dto.OrderRequest;
import org.example.matching.api.dto.OrderResponse;
import org.example.matching.api.service.OrderService;
import org.example.matching.model.Order;
import org.example.matching.orderbook.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
private final OrderService orderService;
private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.processOrder(request);

        if (response.getStatus().equals("REJECTED")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable String id){
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

}}
