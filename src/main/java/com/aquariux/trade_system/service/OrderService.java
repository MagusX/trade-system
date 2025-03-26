package com.aquariux.trade_system.service;

import com.aquariux.trade_system.dto.OpenOrderDto;
import com.aquariux.trade_system.dto.OrderTradeDto;
import com.aquariux.trade_system.entity.PairPriceEntity;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;

public interface OrderService {
    void openOrder(OpenOrderDto dto);

    List<OrderTradeDto> getOrdersWithTradesByOwner(String owner);

    @Async
    @Transactional
    void processOpenTrades(Map<String, PairPriceEntity> pairPriceEntityMap);
}
