package com.dcoffee.controller;

import com.dcoffee.common.Result;
import com.dcoffee.dto.OrderCreateRequest;
import com.dcoffee.service.OrderService;
import com.dcoffee.utils.AuthContext;
import com.dcoffee.vo.OrderView;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping("/user/orders")
    public Result<OrderView> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success(orderService.create(AuthContext.getRequired().getId(), request));
    }

    @GetMapping("/user/orders")
    public Result<List<OrderView>> list() {
        return Result.success(orderService.list(AuthContext.getRequired().getId()));
    }

    @GetMapping("/user/orders/{orderId}")
    public Result<OrderView> get(@PathVariable @Min(1) long orderId) {
        return Result.success(orderService.get(AuthContext.getRequired().getId(), orderId));
    }

    @PostMapping("/user/orders/{orderId}/cancel")
    public Result<OrderView> cancel(@PathVariable @Min(1) long orderId) {
        return Result.success(orderService.cancel(AuthContext.getRequired().getId(), orderId));
    }
}
