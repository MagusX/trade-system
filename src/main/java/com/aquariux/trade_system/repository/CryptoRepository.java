package com.aquariux.trade_system.repository;

import com.aquariux.trade_system.entity.CryptoEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CryptoRepository extends JpaRepository<CryptoEntity, Long> {
    @Query(value = "SELECT * FROM crypto WHERE owner = :owner AND symbol IN (:firstSymbol, :secondSymbol) LIMIT 2", nativeQuery = true)
    List<CryptoEntity> findCryptoPairByOwner(@Param("owner") String owner, @Param("firstSymbol") String firstSymbol, @Param("secondSymbol") String secondSymbol);

    List<CryptoEntity> findAllByOwner(String owner);

    @Modifying
    @Transactional
    @Query(value = "UPDATE crypto SET quantity = :updateQuantity WHERE owner = :owner AND symbol = :symbol", nativeQuery = true)
    int setCryptoQuantity(@Param("owner") String owner, @Param("symbol") String symbol, @Param("updateQuantity") BigDecimal updateQuantity);
}
