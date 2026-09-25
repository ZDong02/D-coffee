package com.dcoffee.controller;

import com.dcoffee.common.Result;
import com.dcoffee.dto.CartAddItemRequest;
import com.dcoffee.dto.CartQuantityRequest;
import com.dcoffee.service.CartService;
import com.dcoffee.utils.AuthContext;
import com.dcoffee.vo.CartView;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@Validated
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/user/cart")
    public Result<CartView> cart(@RequestParam @Min(1) long storeId) {
        return Result.success(cartService.getCart(AuthContext.getRequired().getId(), storeId));
    }

    @PostMapping("/user/cart/items")
    public Result<CartView> addItem(@Valid @RequestBody CartAddItemRequest request) {
        return Result.success(cartService.addItem(AuthContext.getRequired().getId(), request));
    }

    @PatchMapping("/user/cart/items/{itemId}")
    public Result<CartView> updateQuantity(@PathVariable @Min(1) long itemId,
                                           @RequestParam @Min(1) long storeId,
                                           @Valid @RequestBody CartQuantityRequest request) {
        return Result.success(cartService.updateQuantity(AuthContext.getRequired().getId(), storeId,
                itemId, request.getQuantity()));
    }

    @DeleteMapping("/user/cart/items/{itemId}")
    public Result<CartView> removeItem(@PathVariable @Min(1) long itemId,
                                       @RequestParam @Min(1) long storeId) {
        return Result.success(cartService.removeItem(AuthContext.getRequired().getId(), storeId, itemId));
    }
}
