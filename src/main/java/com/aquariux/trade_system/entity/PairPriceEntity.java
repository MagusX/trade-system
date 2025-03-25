package com.aquariux.trade_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pair_price", indexes = {
        @Index(name = "idx_pair_timestamp", columnList = "pair, timestamp")
})
public class PairPriceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String pair;
    @Column(name = "bid_price", precision = 20, scale = 8, nullable = false)
    private BigDecimal bidPrice;
    @Column(name = "bid_quantity", precision = 20, scale = 8, nullable = false)
    private BigDecimal bidQuantity;
    @Column(name = "ask_price", precision = 20, scale = 8, nullable = false)
    private BigDecimal askPrice;
    @Column(name = "ask_quantity", precision = 20, scale = 8, nullable = false)
    private BigDecimal askQuantity;
    private LocalDateTime timestamp;
}
