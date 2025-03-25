package com.aquariux.trade_system.controller;

import com.aquariux.trade_system.dto.OpenOrderDto;
import com.aquariux.trade_system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/open-order")
    public ResponseEntity<?> openOrder(@RequestBody OpenOrderDto dto) {
        orderService.openOrder(dto);
        return ResponseEntity.ok(null);
    }
}
