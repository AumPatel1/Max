package org.example.matching.repository;

import org.example.matching.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByMarketId(Long marketId);
    List<Trade> findByMarketIdOrderByExecutedAtDesc(Long marketId);
}
