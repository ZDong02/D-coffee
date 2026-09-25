package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductUpsertRequest {
    private Long id;
    @NotNull
    private Long categoryId;
    @NotBlank
    @Size(max = 48)
    private String productCode;
    @NotBlank
    @Size(max = 100)
    private String name;
    @Size(max = 1000)
    private String description = "";
    @Size(max = 500)
    private String imageUrl;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;
    @DecimalMin("0.00")
    private BigDecimal originalPrice;
    @NotNull
    @Min(0)
    private Integer stock;
    @NotNull
    private Boolean recommended = false;
    @NotNull
    private Boolean isNew = false;
    @NotNull
    private Integer sort = 0;
}
