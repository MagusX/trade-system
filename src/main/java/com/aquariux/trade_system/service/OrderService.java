package com.aquariux.trade_system.service;

import com.aquariux.trade_system.dto.OpenOrderDto;

public interface OrderService {
    void openOrder(OpenOrderDto dto);
}
