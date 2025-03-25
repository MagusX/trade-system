package com.aquariux.trade_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenOrderDto {
    private String owner;
    private String pair;
    private String side;
    private BigDecimal quantity;
}
