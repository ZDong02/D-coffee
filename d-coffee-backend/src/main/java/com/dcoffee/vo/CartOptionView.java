package com.dcoffee.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartOptionView {
    private Long id;
    private String selectionType;
    private String groupName;
    private String selectedName;
    private BigDecimal priceDelta;
}
