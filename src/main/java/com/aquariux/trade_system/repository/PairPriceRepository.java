package com.aquariux.trade_system.repository;

import com.aquariux.trade_system.entity.PairPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PairPriceRepository extends JpaRepository<PairPriceEntity, Long> {
    @Query(value = "SELECT * FROM pair_price WHERE pair = :pair ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    PairPriceEntity findTopByPairOrderByTimestampDesc(@Param("pair") String pair);
}
