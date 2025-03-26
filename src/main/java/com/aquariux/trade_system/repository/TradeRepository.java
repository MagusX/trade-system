package com.aquariux.trade_system.repository;

import com.aquariux.trade_system.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {
    List<TradeEntity> findByOrderIdIn(List<String> orderIds);

    @Query(value = "SELECT * FROM trade WHERE STATUS = 'OPEN'", nativeQuery = true)
    List<TradeEntity> findAllOpenTrades();
}
