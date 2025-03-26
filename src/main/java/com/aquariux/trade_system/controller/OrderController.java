package com.aquariux.trade_system.controller;

import com.aquariux.trade_system.dto.OpenOrderDto;
import com.aquariux.trade_system.dto.OrderTradeDto;
import com.aquariux.trade_system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{owner}")
    public ResponseEntity<List<OrderTradeDto>> getOrdersWithTrades(@PathVariable String owner) {
        return ResponseEntity.ok(orderService.getOrdersWithTradesByOwner(owner));
    }

    @PostMapping("/open-order")
    public ResponseEntity<?> openOrder(@RequestBody OpenOrderDto dto) {
        try {
            orderService.openOrder(dto);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
        return ResponseEntity.ok(null);
    }
}
