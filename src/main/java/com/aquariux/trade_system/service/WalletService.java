package com.aquariux.trade_system.service;

import com.aquariux.trade_system.entity.CryptoEntity;

import java.util.List;

public interface WalletService {
    List<CryptoEntity> getWallet(String owner);
}
