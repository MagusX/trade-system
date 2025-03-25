package com.aquariux.trade_system.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class TradeUtils {
    public static final String ORDER_STATUS_PARTIALLY_FILLED = "PARTIALLY_FILLED";
    public static final String ORDER_STATUS_FILLED = "FILLED";

    public static final String TRADE_STATUS_OPEN = "OPEN";
    public static final String TRADE_STATUS_COMPLETED = "COMPLETED";
    public static final String TRADE_STATUS_FAILED = "FAILED";

    public static final String SIDE_BUY = "BUY";
    public static final String SIDE_SELL = "SELL";
    public static final String TICKER_SOURCE_BINANCE = "BINANCE";
    public static final String TICKER_SOURCE_HUOBI = "HUOBI";
    public static final String PAIR_ETHUSDT = "ETHUSDT";
    public static final String PAIR_BTCUSDT = "BTCUSDT";

    public static final String SYMBOL_USDT = "USDT";
    public static final String SYMBOL_ETH = "ETH";
    public static final String SYMBOl_BTC = "BTC";

    public static final Map<String, String> PAIR_TARGET_MAP = new HashMap<>();

    static {
        PAIR_TARGET_MAP.put(PAIR_ETHUSDT, SYMBOL_ETH);
        PAIR_TARGET_MAP.put(PAIR_BTCUSDT, SYMBOl_BTC);
    }
}
