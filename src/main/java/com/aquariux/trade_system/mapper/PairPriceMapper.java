package com.aquariux.trade_system.mapper;

import com.aquariux.trade_system.entity.PairPriceEntity;
import com.aquariux.trade_system.util.TradeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public interface PairPriceMapper {
    Logger log = LoggerFactory.getLogger(PairPriceMapper.class);
    ObjectMapper objectMapper = new ObjectMapper();
    Set<String> supportedPairs = Set.of(TradeUtils.PAIR_BTCUSDT, TradeUtils.PAIR_ETHUSDT);

    List<PairPriceEntity> fromResponse(String jsonResponse);
}
