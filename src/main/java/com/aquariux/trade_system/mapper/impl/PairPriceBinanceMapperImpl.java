package com.aquariux.trade_system.mapper.impl;

import com.aquariux.trade_system.entity.PairPriceEntity;
import com.aquariux.trade_system.mapper.PairPriceMapper;
import com.aquariux.trade_system.util.TradeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component(value = TradeUtils.TICKER_SOURCE_BINANCE)
public class PairPriceBinanceMapperImpl implements PairPriceMapper {
    public List<PairPriceEntity> fromResponse(String jsonResponse) {
        List<PairPriceEntity> results = new ArrayList<>();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to map PairPrice from response", e);
            return null;
        }

        for (JsonNode dataNode : rootNode) {
            String pair = dataNode.get("symbol").asText().toUpperCase();
            if (!supportedPairs.contains(pair)) continue;

            BigDecimal bidPrice = new BigDecimal(dataNode.get("bidPrice").asText());
            BigDecimal bidQuantity = new BigDecimal(dataNode.get("bidQty").asText());
            BigDecimal askPrice = new BigDecimal(dataNode.get("askPrice").asText());
            BigDecimal askQuantity = new BigDecimal(dataNode.get("askQty").asText());

            LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);

            results.add(new PairPriceEntity(null, pair, bidPrice, bidQuantity, askPrice, askQuantity, TradeUtils.TICKER_SOURCE_BINANCE, timestamp));
        }

        return results;
    }
}
