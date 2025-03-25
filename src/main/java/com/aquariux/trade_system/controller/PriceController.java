package com.aquariux.trade_system.controller;

import com.aquariux.trade_system.dto.LatestPriceDto;
import com.aquariux.trade_system.entity.PairPriceEntity;
import com.aquariux.trade_system.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/price")
public class PriceController {
    private final PriceService priceService;

    @Autowired
    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/latest/{pair}")
    public ResponseEntity<PairPriceEntity> getLatestPrice(@PathVariable("pair") String pair) {
        PairPriceEntity pairPriceEntity = priceService.getLatestPrice(pair);
        return ResponseEntity.ok(pairPriceEntity);
    }
}
