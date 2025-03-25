package com.aquariux.trade_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trade_order", indexes = {
        @Index(name = "idx_owner_createdAt", columnList = "owner, created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String owner;
    private String pair;
    private String side;
    private String status;
    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal quantity;
    @Column(name = "filled_quantity", precision = 20, scale = 8, nullable = false)
    private BigDecimal filledQuantity;
    @Column(name = "unfilled_quantity", precision = 20, scale = 8, nullable = false)
    private BigDecimal unfilledQuantity;
    @Column(name = "market_price", precision = 20, scale = 8, nullable = false)
    private BigDecimal marketPrice;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
