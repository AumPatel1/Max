package org.example.matching.repository;

import org.example.matching.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketRepository extends JpaRepository<Market, Long> {
    List<Market> findByEventId(Long eventId);
    List<Market> findByStatus(Market.MarketStatus status);
}
