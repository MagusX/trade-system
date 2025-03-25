package com.aquariux.trade_system.mapper;

import com.aquariux.trade_system.entity.PairPriceEntity;
import com.aquariux.trade_system.util.TradeUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class PairPriceBinanceMapperImpl implements PairPriceMapper {
    public PairPriceEntity fromResponse(String jsonResponse) throws Exception {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode dataNode = rootNode.get(0);
        if (dataNode.isMissingNode()) {
            throw new IllegalArgumentException("Invalid Binance response format");
        }

        String pair = dataNode.get("symbol").asText().toUpperCase();
        BigDecimal bidPrice = new BigDecimal(dataNode.get("bidPrice").asText());
        BigDecimal bidQuantity = new BigDecimal(dataNode.get("bidQty").asText());
        BigDecimal askPrice = new BigDecimal(dataNode.get("askPrice").asText());
        BigDecimal askQuantity = new BigDecimal(dataNode.get("askQty").asText());

        LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);

        return new PairPriceEntity(null, pair, bidPrice, bidQuantity, askPrice, askQuantity, TradeUtils.TICKER_SOURCE_BINANCE, timestamp);
    }
}
