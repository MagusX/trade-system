package com.aquariux.trade_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trade", indexes = {
        @Index(name = "idx_orderId", columnList = "order_id")
})
@EntityListeners(AuditingEntityListener.class)
public class TradeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "order_id")
    private Long orderId;
    private String status;
    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal quantity;
    @Column(name = "market_price", precision = 20, scale = 8, nullable = false)
    private BigDecimal marketPrice;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
