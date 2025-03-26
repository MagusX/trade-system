package com.aquariux.trade_system.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTradeDto {
    private String id;
    private String owner;
    private String pair;
    private String side;
    private String status;
    private BigDecimal quantity;
    private BigDecimal filledQuantity;
    private BigDecimal unfilledQuantity;
    private BigDecimal marketPrice;
    private LocalDateTime createdAt;
    private List<TradeDto> trades;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeDto {
        private Long id;
        private String orderId;
        private String status;
        private BigDecimal quantity;
        private BigDecimal marketPrice;
        private LocalDateTime createdAt;
    }
}
