package com.aquariux.trade_system.service.impl;

import com.aquariux.trade_system.entity.PairPriceEntity;
import com.aquariux.trade_system.mapper.PairPriceMapper;
import com.aquariux.trade_system.repository.PairPriceRepository;
import com.aquariux.trade_system.util.TradeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class TickerServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(TickerServiceImpl.class);
    @Value("${app.ticker-api.binance}")
    private String binanceUrl;
    @Value("${app.ticker-api.huobi}")
    private String huobiUrl;
    private final RestTemplate restTemplate;
    private final PairPriceMapper binanceMapper;
    private final PairPriceMapper huobiMapper;
    private final PairPriceRepository pairPriceRepository;


    @Autowired
    public TickerServiceImpl(RestTemplate restTemplate,
                             @Qualifier(value = TradeUtils.TICKER_SOURCE_BINANCE) PairPriceMapper binanceMapper,
                             @Qualifier(value = TradeUtils.TICKER_SOURCE_HUOBI) PairPriceMapper huobiMapper,
                             PairPriceRepository pairPriceRepository) {
        this.restTemplate = restTemplate;
        this.binanceMapper = binanceMapper;
        this.huobiMapper = huobiMapper;
        this.pairPriceRepository = pairPriceRepository;
    }

    @Async
    public CompletableFuture<List<PairPriceEntity>> fetchPrice(String url, PairPriceMapper pairPriceMapper) {
        // TODO: Handle failed response
        String response = restTemplate.getForObject(url, String.class);
        return CompletableFuture.completedFuture(pairPriceMapper.fromResponse(response));
    }

    @Scheduled(fixedRate = 10000) // 10 seconds
    public void schedulePriceUpdate() {
        CompletableFuture<List<PairPriceEntity>> binancePairsFuture = fetchPrice(binanceUrl, binanceMapper);
        CompletableFuture<List<PairPriceEntity>> huobiPairsFuture = fetchPrice(huobiUrl, huobiMapper);

        CompletableFuture.allOf(binancePairsFuture, huobiPairsFuture).thenRun(() -> {
            try {
                Map<String, PairPriceEntity> bestPriceMap = new HashMap<>();
                List<PairPriceEntity> binancePairs = binancePairsFuture.get();
                List<PairPriceEntity> huobiPairs = binancePairsFuture.get();

                if (binancePairs != null) {
                    for (PairPriceEntity pair : binancePairs) {
                        bestPriceMap.put(pair.getPair(), pair);
                    }
                }
                if (huobiPairs != null) {
                    for (PairPriceEntity pair : huobiPairs) {
                        PairPriceEntity bestPair = bestPriceMap.get(pair.getPair());
                        if (bestPair == null) {
                            bestPriceMap.put(pair.getPair(), pair);
                            continue;
                        }

                        // choose the best ask & bid price
                        if (pair.getAskPrice().compareTo(bestPair.getAskPrice()) < 0) {
                            bestPair.setAskPrice(pair.getAskPrice());
                        }
                        if (pair.getBidPrice().compareTo(bestPair.getBidPrice()) > 0) {
                            bestPair.setBidPrice(pair.getBidPrice());
                        }
                    }
                }

                if (bestPriceMap.isEmpty()) return;

                pairPriceRepository.saveAll(bestPriceMap.values());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

//    @Scheduled(fixedRate = 10000) // 10 seconds
//    public void schedulePriceUpdate() {
//        CompletableFuture<List<PairPriceEntity>> binancePairsFuture = fetchPrice(binanceUrl, binanceMapper);
//        CompletableFuture<List<PairPriceEntity>> huobiPairsFuture = fetchPrice(huobiUrl, huobiMapper);
//
//        CompletableFuture.allOf(binancePairsFuture, huobiPairsFuture)
//        .thenRun(() -> {
//            try {
//                Map<String, PairPriceEntity> bestPriceMap = new HashMap<>();
//                List<PairPriceEntity> binancePairs = binancePairsFuture.get();
//                List<PairPriceEntity> huobiPairs = binancePairsFuture.get();
//
////                if (binancePairs != null) {
////                    for (PairPriceEntity pair : binancePairs) {
////                        bestPriceMap.put(pair.getPair(), pair);
////                    }
////                }
////                if (huobiPairs != null) {
////                    for (PairPriceEntity pair : huobiPairs) {
////                        var binancePair = bestPriceMap.get(pair.getPair());
////                        if (binancePair == null) {
////                            bestPriceMap.put(pair.getPair(), pair);
////                        } else if (pair.getAskPrice().compareTo(binancePair.getAskPrice()) < 0) {
////                            bestPriceMap.put(pair.getPair(), pair);
////                        }
////                    }
////                }
//
////                if ()
//
//            } catch (InterruptedException | ExecutionException e) {
//                throw new RuntimeException(e);
//            }
//
//        });
//    }
}
