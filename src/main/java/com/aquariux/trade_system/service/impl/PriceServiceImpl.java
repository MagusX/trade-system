package com.aquariux.trade_system.service.impl;

import com.aquariux.trade_system.entity.PairPriceEntity;
import com.aquariux.trade_system.repository.PairPriceRepository;
import com.aquariux.trade_system.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PriceServiceImpl implements PriceService {
    private static final Logger log = LoggerFactory.getLogger(PriceServiceImpl.class);
    private final PairPriceRepository pairPriceRepository;

    @Autowired
    public PriceServiceImpl(PairPriceRepository pairPriceRepository) {
        this.pairPriceRepository = pairPriceRepository;
    }

    public PairPriceEntity getLatestPrice(String pair) {
        return pairPriceRepository.findTopByPairOrderByTimestampDesc(pair);
    }
}
