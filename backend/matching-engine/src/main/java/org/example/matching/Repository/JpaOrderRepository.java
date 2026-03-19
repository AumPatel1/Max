package org.example.matching.Repository;

import org.example.matching.api.dto.Order;
import org.example.matching.api.dto.OrderStatus;
import org.example.matching.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, String> {

    // 1. Fetch a user's full trade history
    List<Order> findByUserId(String userId);

    // 2. Fetch only "OPEN" orders for a user (needed for the UI 'Active' tab)
    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);

    // 3. Fetch orders for a specific market (e.g., "FED_MAR_Y")
    List<Order> findByInstrument(String instrument);

    // 4. Fetch the Order Book for a specific market from the DB (Recovery Logic)
    // Bids: Sorted by Price High -> Low
    List<Order> findByInstrumentAndSideAndStatusOrderByPriceDesc(
            String instrument, String side, OrderStatus status);

    // Asks: Sorted by Price Low -> High
    List<Order> findByInstrumentAndSideAndStatusOrderByPriceAsc(
            String instrument, String side, OrderStatus status);

    // 5. Delete or Archive old cancelled/filled orders
    void deleteByStatus(OrderStatus status);
}