package com.dcoffee.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductView {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String productCode;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer salesCount;
    private Boolean recommended;
    private Boolean isNew;
    private String status;
    private Integer sort;
    private List<ProductOptionView> options;
    private List<ProductExtraView> extras;
}
