package com.dcoffee.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStockReservationView {
    private Long orderItemId;
    private String reservationType;
    private Long productId;
    private Long extraId;
    private Integer quantity;
}
