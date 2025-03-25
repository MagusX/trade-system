package com.aquariux.trade_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "crypto", indexes = {
    @Index(name = "idx_owner_symbol", columnList = "owner, symbol")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_owner_symbol", columnNames = {"owner", "symbol"})
})
public class CryptoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String owner;
    private String symbol;
    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal quantity;
}
