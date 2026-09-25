package com.dcoffee.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderRecord {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long storeId;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String remark;
}
