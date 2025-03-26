package com.aquariux.trade_system.repository;

import com.aquariux.trade_system.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    OrderEntity findOrderById(String id);

    @Modifying
    @Query(value = "UPDATE trade_order SET filled_quantity = quantity, unfilled_quantity = 0, status = :status WHERE id = :orderId", nativeQuery = true)
    int updateOrder(@Param("status") String status, @Param("orderId") String orderId);
}
