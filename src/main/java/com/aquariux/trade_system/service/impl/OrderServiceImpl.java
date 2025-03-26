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
import java.util.Map;

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

    @Async
    @Transactional
    @Override
    public void processOpenTrades(Map<String, PairPriceEntity> pairPriceEntityMap) {
        List<TradeEntity> tradeEntities = tradeRepository.findAllOpenTrades();
        for (TradeEntity trade : tradeEntities) {
            PairPriceEntity pairPriceEntity = pairPriceEntityMap.get(trade.getPair());

            boolean isBuy = TradeUtils.SIDE_BUY.equals(trade.getSide());
            CryptoEntity[] cryptoPair = getCryptoPair(trade.getOwner(), trade.getPair());
            CryptoEntity balance = isBuy ? cryptoPair[0] : cryptoPair[1];

            BigDecimal marketPrice = isBuy ? pairPriceEntity.getAskPrice() : pairPriceEntity.getBidPrice();
            BigDecimal matchQuantity = isBuy ? pairPriceEntity.getAskQuantity() : pairPriceEntity.getBidQuantity();

            trade.setMarketPrice(marketPrice);

            if (balance.getQuantity().compareTo(isBuy ? trade.getQuantity().multiply(marketPrice) : trade.getQuantity()) < 0) {
                trade.setStatus(TradeUtils.TRADE_STATUS_FAILED);
                tradeRepository.save(trade);
                continue;
            }

            BigDecimal remainingQuantity = trade.getQuantity().subtract(matchQuantity);

            if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) { // Partially filled
                trade.setQuantity(matchQuantity);
                trade.setStatus(TradeUtils.TRADE_STATUS_COMPLETED);

                TradeEntity remainingTrade = new TradeEntity();
                remainingTrade.setOrderId(trade.getOrderId());
                remainingTrade.setSide(trade.getSide());
                remainingTrade.setOwner(trade.getOwner());
                remainingTrade.setPair(trade.getPair());
                remainingTrade.setMarketPrice(BigDecimal.ZERO);
                remainingTrade.setQuantity(remainingQuantity);
                remainingTrade.setStatus(TradeUtils.TRADE_STATUS_OPEN);

                updateWalletBalance(trade.getOwner(), marketPrice, matchQuantity, cryptoPair[0], cryptoPair[1], isBuy);

                OrderEntity orderEntity = orderRepository.findOrderById(trade.getOrderId());
                orderEntity.setFilledQuantity(orderEntity.getFilledQuantity().add(matchQuantity));
                orderEntity.setUnfilledQuantity(remainingQuantity);

                orderRepository.save(orderEntity);
                tradeRepository.saveAll(List.of(trade, remainingTrade));
            } else { // Fully filled
                trade.setStatus(TradeUtils.TRADE_STATUS_COMPLETED);
                updateWalletBalance(trade.getOwner(), marketPrice, trade.getQuantity(), cryptoPair[0], cryptoPair[1], isBuy);

                orderRepository.updateOrder(TradeUtils.ORDER_STATUS_FILLED, trade.getOrderId());
                tradeRepository.save(trade);
            }
        }
    }

    @Override
    public void openOrder(OpenOrderDto dto) {
        PairPriceEntity pairPriceEntity = priceService.getLatestPrice(dto.getPair());
        CryptoEntity[] usdtOtherPair = getCryptoPair(dto.getOwner(), dto.getPair());
        CryptoEntity usdtEntity = usdtOtherPair[0];
        CryptoEntity otherCryptoEntity = usdtOtherPair[1];

        if (TradeUtils.SIDE_BUY.equals(dto.getSide())) {
            if (usdtEntity.getQuantity().compareTo(dto.getQuantity().multiply(pairPriceEntity.getAskPrice())) < 0) {
                throw new RuntimeException("Insufficient balance USDT");
            }

            openOrder(dto, pairPriceEntity, usdtEntity, otherCryptoEntity, true);
        } else if (TradeUtils.SIDE_SELL.equals(dto.getSide())) {
            String targetSymbol = TradeUtils.PAIR_TARGET_MAP.get(dto.getPair());
            if (otherCryptoEntity.getQuantity().compareTo(dto.getQuantity()) < 0) {
                throw new RuntimeException("Insufficient balance " + targetSymbol);
            }

            openOrder(dto, pairPriceEntity, usdtEntity, otherCryptoEntity, false);
        } else {
            throw new RuntimeException("INVALID ORDER SIDE");
        }
    }

    private CryptoEntity[] getCryptoPair(String owner, String pair) {
        List<CryptoEntity> cryptoPair = cryptoRepository.findCryptoPairByOwner(owner, TradeUtils.SYMBOL_USDT, TradeUtils.PAIR_TARGET_MAP.get(pair));

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

        return new CryptoEntity[]{usdtEntity, otherCryptoEntity};
    }

    @Async
    @Transactional
    private void openOrder(OpenOrderDto dto, PairPriceEntity pairPriceEntity, CryptoEntity usdtEntity, CryptoEntity otherCryptoEntity, boolean isBuyOrder) {
        BigDecimal marketPrice = isBuyOrder ? pairPriceEntity.getAskPrice() : pairPriceEntity.getBidPrice();
        BigDecimal matchQuantity = isBuyOrder ? pairPriceEntity.getAskQuantity() : pairPriceEntity.getBidQuantity();
        BigDecimal remainingOrderQuantity = dto.getQuantity().subtract(matchQuantity);

        // Create order
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(IDUtils.generateShortUUID());
        orderEntity.setOwner(dto.getOwner());
        orderEntity.setPair(dto.getPair());
        orderEntity.setSide(dto.getSide());
        orderEntity.setQuantity(dto.getQuantity());
        orderEntity.setMarketPrice(marketPrice);

        // Create trade
        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.setOrderId(orderEntity.getId());
        tradeEntity.setOwner(dto.getOwner());
        tradeEntity.setPair(dto.getPair());
        tradeEntity.setSide(dto.getSide());
        tradeEntity.setMarketPrice(marketPrice);

        if (remainingOrderQuantity.compareTo(BigDecimal.ZERO) <= 0) { // Fully filled
            handleFullyFilledOrder(orderEntity, tradeEntity, dto, marketPrice, usdtEntity, otherCryptoEntity, isBuyOrder);
            return;
        }

        // Partially filled
        handlePartiallyFilledOrder(orderEntity, tradeEntity, remainingOrderQuantity, dto, matchQuantity, usdtEntity, otherCryptoEntity, isBuyOrder);
    }

    private void handleFullyFilledOrder(OrderEntity orderEntity, TradeEntity tradeEntity, OpenOrderDto dto, BigDecimal marketPrice, CryptoEntity usdtEntity, CryptoEntity otherCryptoEntity, boolean isBuyOrder) {
        orderEntity.setStatus(TradeUtils.ORDER_STATUS_FILLED);
        orderEntity.setFilledQuantity(dto.getQuantity());
        orderEntity.setUnfilledQuantity(BigDecimal.ZERO);

        tradeEntity.setQuantity(dto.getQuantity());
        tradeEntity.setStatus(TradeUtils.TRADE_STATUS_COMPLETED);

        updateWalletBalance(dto.getOwner(), marketPrice, dto.getQuantity(), usdtEntity, otherCryptoEntity, isBuyOrder);

        orderRepository.save(orderEntity);
        tradeRepository.save(tradeEntity);
    }

    private void handlePartiallyFilledOrder(OrderEntity orderEntity, TradeEntity tradeEntity, BigDecimal remainingOrderQuantity, OpenOrderDto dto, BigDecimal matchQuantity, CryptoEntity usdtEntity, CryptoEntity otherCryptoEntity, boolean isBuyOrder) {
        orderEntity.setStatus(TradeUtils.ORDER_STATUS_PARTIALLY_FILLED);
        orderEntity.setFilledQuantity(matchQuantity);
        orderEntity.setUnfilledQuantity(remainingOrderQuantity);

        tradeEntity.setQuantity(matchQuantity);
        tradeEntity.setStatus(TradeUtils.TRADE_STATUS_COMPLETED);

        TradeEntity remainingTradeEntity = new TradeEntity();
        remainingTradeEntity.setOrderId(orderEntity.getId());
        remainingTradeEntity.setOwner(dto.getOwner());
        remainingTradeEntity.setPair(dto.getPair());
        remainingTradeEntity.setSide(dto.getSide());
        remainingTradeEntity.setMarketPrice(BigDecimal.ZERO);
        remainingTradeEntity.setQuantity(remainingOrderQuantity);
        remainingTradeEntity.setStatus(TradeUtils.TRADE_STATUS_OPEN);

        updateWalletBalance(dto.getOwner(), tradeEntity.getMarketPrice(), matchQuantity, usdtEntity, otherCryptoEntity, isBuyOrder);

        orderRepository.save(orderEntity);
        tradeRepository.saveAll(List.of(tradeEntity, remainingTradeEntity));
    }

    private void updateWalletBalance(String owner, BigDecimal marketPrice, BigDecimal quantity, CryptoEntity usdtEntity, CryptoEntity otherCryptoEntity, boolean isBuyOrder) {
        if (isBuyOrder) {
            cryptoRepository.setCryptoQuantity(owner, TradeUtils.SYMBOL_USDT, usdtEntity.getQuantity().subtract(quantity.multiply(marketPrice)));
            cryptoRepository.setCryptoQuantity(owner, otherCryptoEntity.getSymbol(), otherCryptoEntity.getQuantity().add(quantity));
        } else {
            cryptoRepository.setCryptoQuantity(owner, otherCryptoEntity.getSymbol(), otherCryptoEntity.getQuantity().subtract(quantity));
            cryptoRepository.setCryptoQuantity(owner, TradeUtils.SYMBOL_USDT, usdtEntity.getQuantity().add(marketPrice.multiply(quantity)));
        }
    }
}
