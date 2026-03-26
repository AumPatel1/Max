package org.example.matching.repository;

import org.example.matching.entity.MarketEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketEventRepository extends JpaRepository<MarketEventEntity, String> {

    List<MarketEventEntity> findByStatusNot(String status);

    @Modifying
    @Query("UPDATE MarketEventEntity e SET e.status = :status WHERE e.id = :id")
    void updateStatus(@Param("id") String id, @Param("status") String status);
}
