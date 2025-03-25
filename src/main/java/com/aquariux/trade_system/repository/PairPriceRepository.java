package com.aquariux.trade_system.repository;

import com.aquariux.trade_system.entity.PairPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PairPriceRepository extends JpaRepository<PairPriceEntity, Long> {
}
