package com.aquariux.trade_system.service;

import com.aquariux.trade_system.entity.PairPriceEntity;

public interface PriceService {
    PairPriceEntity getLatestPrice(String pair);
}
