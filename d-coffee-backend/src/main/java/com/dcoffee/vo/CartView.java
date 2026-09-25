package com.dcoffee.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartView {
    private Long storeId;
    private String storeName;
    private String storeStatus;
    private List<CartItemView> items;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
}
