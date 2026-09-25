package com.dcoffee.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartItemView {
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal basePrice;
    private BigDecimal unitPrice;
    private Integer quantity;
    private List<CartOptionView> options;
}
