package org.example.matching.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matching.api.dto.OrderRequest;
import org.example.matching.api.dto.OrderResponse;
import org.example.matching.api.service.OrderService;
import org.example.matching.model.Order;
import org.example.matching.orderbook.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    /**
     * POST /api/orders
     * Places an order for the authenticated user.
     *
     * userId is extracted from the JWT and set programmatically on the request —
     * any userId field the client sends in the body is silently overwritten.
     * This prevents a user from placing orders on behalf of another account.
     *
     * Flow:
     *   JwtAuthFilter → sets SecurityContextHolder
     *         ↓
     *   authentication.getName() → returns username (= userId in this system)
     *         ↓
     *   request.setUserId(username) → injected before reaching OrderService
     *         ↓
     *   OrderService.processOrder(request) → RiskManager, MatchingEngine, settlement
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        // Extract userId from the JWT — never trust what the client sends as userId
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        request.setUserId(username);

        OrderResponse response = orderService.processOrder(request);

        if (response.getStatus().equals("REJECTED")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
