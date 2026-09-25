package com.dcoffee.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderView {
    private Long id;
    private String orderNo;
    private Long storeId;
    private String storeName;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String remark;
    private Date createTime;
    private List<OrderItemView> items;

    @Getter
    @Setter
    public static class OrderItemView {
        private Long id;
        private Long productId;
        private String productName;
        private String imageUrl;
        private BigDecimal basePrice;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        private List<CartOptionView> options;
    }
}
