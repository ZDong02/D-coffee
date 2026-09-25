package com.dcoffee.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductExtraView {
    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Integer sort;
}
