package org.example.matching.repository;

import org.example.matching.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

    // Load all open/partial orders for rebuilding the in-memory book on startup
    @Query("SELECT o FROM OrderEntity o WHERE o.status IN ('OPEN', 'PARTIALLY_FILLED') ORDER BY o.timestamp ASC")
    List<OrderEntity> findAllActive();

    @Modifying
    @Query("UPDATE OrderEntity o SET o.remainingQty = o.remainingQty - :qty WHERE o.id = :id")
    void decrementRemainingQty(@Param("id") String id, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE OrderEntity o SET o.status = :status WHERE o.id = :id")
    void updateStatus(@Param("id") String id, @Param("status") String status);

    @Modifying
    @Query("UPDATE OrderEntity o SET o.remainingQty = :qty, o.status = :status WHERE o.id = :id")
    void updateRemainingQtyAndStatus(@Param("id") String id, @Param("qty") int qty, @Param("status") String status);
}
