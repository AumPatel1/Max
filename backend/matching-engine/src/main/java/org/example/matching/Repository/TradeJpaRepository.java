package org.example.matching.Repository;

import org.example.matching.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeJpaRepository extends JpaRepository<TradeEntity, String> {
    List<TradeEntity> findByInstrumentOrderByTimestampDesc(String instrument);
}
