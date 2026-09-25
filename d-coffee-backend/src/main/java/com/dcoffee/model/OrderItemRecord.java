package com.dcoffee.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRecord {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal basePrice;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
