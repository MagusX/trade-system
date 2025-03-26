package com.aquariux.trade_system.repository;

import com.aquariux.trade_system.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    @Modifying
    @Query("UPDATE trade_order SET status = :status WHERE id = :orderId")
    int updateOrderStatus(@Param("status") String status, @Param("orderId") String orderId);
}
