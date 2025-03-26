package com.aquariux.trade_system.service.impl;

import com.aquariux.trade_system.entity.CryptoEntity;
import com.aquariux.trade_system.repository.CryptoRepository;
import com.aquariux.trade_system.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {
    private final CryptoRepository cryptoRepository;

    @Autowired
    public WalletServiceImpl(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public List<CryptoEntity> getWallet(String owner) {
        return cryptoRepository.findAllByOwner(owner);
    }
}
