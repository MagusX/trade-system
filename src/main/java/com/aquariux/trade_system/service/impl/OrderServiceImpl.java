package com.aquariux.trade_system.service.impl;

import com.aquariux.trade_system.dto.OpenOrderDto;
import com.aquariux.trade_system.entity.CryptoEntity;
import com.aquariux.trade_system.entity.OrderEntity;
import com.aquariux.trade_system.entity.PairPriceEntity;
import com.aquariux.trade_system.entity.TradeEntity;
import com.aquariux.trade_system.repository.CryptoRepository;
import com.aquariux.trade_system.repository.OrderRepository;
import com.aquariux.trade_system.repository.TradeRepository;
import com.aquariux.trade_system.service.OrderService;
import com.aquariux.trade_system.service.PriceService;
import com.aquariux.trade_system.util.IDUtils;
import com.aquariux.trade_system.util.TradeUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final PriceService priceService;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final CryptoRepository cryptoRepository;

    @Autowired
    public OrderServiceImpl(PriceService priceService,
                            OrderRepository orderRepository,
                            TradeRepository tradeRepository,
                            CryptoRepository cryptoRepository) {
        this.priceService = priceService;
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.cryptoRepository = cryptoRepository;
    }

    public void openOrder(OpenOrderDto dto) {
        PairPriceEntity pairPriceEntity = priceService.getLatestPrice(dto.getPair());
        List<CryptoEntity> cryptoPair = cryptoRepository.findCryptoPairByOwner(dto.getOwner(), TradeUtils.SYMBOL_USDT, TradeUtils.PAIR_TARGET_MAP.get(dto.getPair()));

        if (cryptoPair.isEmpty() || cryptoPair.size() < 2) {
            throw new RuntimeException("Failed to check crypto balance");
        }

        CryptoEntity usdtEntity;
        CryptoEntity otherCryptoEntity;
        if (TradeUtils.SYMBOL_USDT.equals(cryptoPair.get(0).getSymbol())) {
            usdtEntity = cryptoPair.get(0);
            otherCryptoEntity = cryptoPair.get(1);
        } else {
            usdtEntity = cryptoPair.get(1);
            otherCryptoEntity = cryptoPair.get(0);
        }

        if (TradeUtils.SIDE_BUY.equals(dto.getSide())) {
            if (usdtEntity.getQuantity().compareTo(dto.getQuantity().multiply(pairPriceEntity.getAskQuantity())) < 0) {
                throw new RuntimeException("Insufficient balance USDT");
            }

            openBuyOrder(dto, pairPriceEntity, usdtEntity, otherCryptoEntity);
        } else if (TradeUtils.SIDE_SELL.equals(dto.getSide())) {
            String targetSymbol = TradeUtils.PAIR_TARGET_MAP.get(dto.getPair());
            if (otherCryptoEntity.getQuantity().compareTo(dto.getQuantity()) < 0) {
                throw new RuntimeException("Insufficient balance " + targetSymbol);
            }

            openSellOrder(dto, pairPriceEntity, usdtEntity, otherCryptoEntity);
        } else {
            throw new RuntimeException("INVALID ORDER SIDE");
        }
    }

    @Async
    @Transactional
    private void openBuyOrder(OpenOrderDto dto, PairPriceEntity pairPriceEntity, CryptoEntity usdtEntity, CryptoEntity otherCryptoEntity) {
        // create order
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(IDUtils.generateShortUUID());
        orderEntity.setOwner(dto.getOwner());
        orderEntity.setPair(dto.getPair());
        orderEntity.setSide(dto.getSide());
        orderEntity.setQuantity(dto.getQuantity());
        orderEntity.setMarketPrice(pairPriceEntity.getAskPrice());

        // match max trade -> new trade status COMPLETED
        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.setOrderId(orderEntity.getId());
        tradeEntity.setMarketPrice(pairPriceEntity.getAskPrice());

        BigDecimal remainingOrderQuantity = dto.getQuantity().subtract(pairPriceEntity.getAskQuantity());

        // fully filled
        if (remainingOrderQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            orderEntity.setStatus(TradeUtils.ORDER_STATUS_FILLED);
            orderEntity.setFilledQuantity(dto.getQuantity());
            orderEntity.setUnfilledQuantity(new BigDecimal(0));

            tradeEntity.setQuantity(dto.getQuantity());
            tradeEntity.setStatus(TradeUtils.TRADE_STATUS_COMPLETED);

            // update crypto wallet balance
            cryptoRepository.setCryptoQuantity(dto.getOwner(), TradeUtils.SYMBOL_USDT,
                    usdtEntity.getQuantity().subtract(dto.getQuantity().multiply(pairPriceEntity.getAskPrice())));
            cryptoRepository.setCryptoQuantity(dto.getOwner(), otherCryptoEntity.getSymbol(), otherCryptoEntity.getQuantity().add(dto.getQuantity()));

            orderRepository.save(orderEntity);
            tradeRepository.save(tradeEntity);
            return;
        }

        // partially filled
        orderEntity.setStatus(TradeUtils.ORDER_STATUS_PARTIALLY_FILLED);
        orderEntity.setFilledQuantity(pairPriceEntity.getAskQuantity());
        orderEntity.setUnfilledQuantity(remainingOrderQuantity);

        tradeEntity.setQuantity(pairPriceEntity.getAskQuantity());
        tradeEntity.setStatus(TradeUtils.TRADE_STATUS_COMPLETED);

        // create new trade for the remaining ask quantity for processing next price tick
        TradeEntity remainingTradeEntity = new TradeEntity();
        remainingTradeEntity.setOrderId(orderEntity.getId());
        remainingTradeEntity.setMarketPrice(BigDecimal.ZERO);
        remainingTradeEntity.setQuantity(remainingOrderQuantity);
        remainingTradeEntity.setStatus(TradeUtils.TRADE_STATUS_OPEN);

        // update crypto wallet balance
        cryptoRepository.setCryptoQuantity(dto.getOwner(), TradeUtils.SYMBOL_USDT,
                usdtEntity.getQuantity().subtract(pairPriceEntity.getAskQuantity().multiply(pairPriceEntity.getAskPrice())));
        cryptoRepository.setCryptoQuantity(dto.getOwner(), otherCryptoEntity.getSymbol(),
                otherCryptoEntity.getQuantity().add(pairPriceEntity.getAskQuantity()));

        orderRepository.save(orderEntity);
        tradeRepository.saveAll(List.of(tradeEntity, remainingTradeEntity));
    }

    @Async
    @Transactional
    private void openSellOrder(OpenOrderDto dto, PairPriceEntity pairPriceEntity, CryptoEntity usdtEntity, CryptoEntity otherCryptoEntity) {
        // create order
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(IDUtils.generateShortUUID());
        orderEntity.setOwner(dto.getOwner());
        orderEntity.setPair(dto.getPair());
        orderEntity.setSide(dto.getSide());
        orderEntity.setQuantity(dto.getQuantity());
        orderEntity.setMarketPrice(pairPriceEntity.getBidPrice());

        // match max trade -> new trade status COMPLETED
        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.setOrderId(orderEntity.getId());
        tradeEntity.setMarketPrice(pairPriceEntity.getBidPrice());

        BigDecimal remainingOrderQuantity = dto.getQuantity().subtract(pairPriceEntity.getBidQuantity());

        // fully filled
        if (remainingOrderQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            orderEntity.setStatus(TradeUtils.ORDER_STATUS_FILLED);
            orderEntity.setFilledQuantity(dto.getQuantity());
            orderEntity.setUnfilledQuantity(new BigDecimal(0));

            tradeEntity.setQuantity(dto.getQuantity());
            tradeEntity.setStatus(TradeUtils.TRADE_STATUS_COMPLETED);

            // update crypto wallet balance
            cryptoRepository.setCryptoQuantity(dto.getOwner(), otherCryptoEntity.getSymbol(), otherCryptoEntity.getQuantity().subtract(dto.getQuantity()));
            cryptoRepository.setCryptoQuantity(dto.getOwner(), TradeUtils.SYMBOL_USDT,
                    usdtEntity.getQuantity().add(pairPriceEntity.getBidPrice().multiply(dto.getQuantity())));

            orderRepository.save(orderEntity);
            tradeRepository.save(tradeEntity);
            return;
        }

        // partially filled
        orderEntity.setStatus(TradeUtils.ORDER_STATUS_PARTIALLY_FILLED);
        orderEntity.setFilledQuantity(pairPriceEntity.getAskQuantity());
        orderEntity.setUnfilledQuantity(remainingOrderQuantity);

        tradeEntity.setQuantity(pairPriceEntity.getBidQuantity());
        tradeEntity.setStatus(TradeUtils.TRADE_STATUS_COMPLETED);

        // create new trade for the remaining ask quantity for processing next price tick
        TradeEntity remainingTradeEntity = new TradeEntity();
        remainingTradeEntity.setOrderId(orderEntity.getId());
        remainingTradeEntity.setMarketPrice(BigDecimal.ZERO);
        remainingTradeEntity.setQuantity(remainingOrderQuantity);
        remainingTradeEntity.setStatus(TradeUtils.TRADE_STATUS_OPEN);

        // update crypto wallet balance
        cryptoRepository.setCryptoQuantity(dto.getOwner(), otherCryptoEntity.getSymbol(), otherCryptoEntity.getQuantity().subtract(pairPriceEntity.getBidQuantity()));
        cryptoRepository.setCryptoQuantity(dto.getOwner(), TradeUtils.SYMBOL_USDT,
                usdtEntity.getQuantity().add(pairPriceEntity.getBidPrice().multiply(pairPriceEntity.getBidQuantity())));

        orderRepository.save(orderEntity);
        tradeRepository.saveAll(List.of(tradeEntity, remainingTradeEntity));
    }
}
