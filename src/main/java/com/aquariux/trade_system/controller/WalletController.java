package com.aquariux.trade_system.controller;

import com.aquariux.trade_system.entity.CryptoEntity;
import com.aquariux.trade_system.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{owner}")
    public ResponseEntity<List<CryptoEntity>> getWallet(@PathVariable("owner") String owner) {
        return ResponseEntity.ok(walletService.getWallet(owner));
    }
}
