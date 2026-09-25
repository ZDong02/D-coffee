package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductUpdateRequest {
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
    private Boolean recommended = false;
    @NotNull
    private Boolean isNew = false;
    @NotNull
    private Integer sort = 0;

    public ProductUpsertRequest toProductRequest() {
        ProductUpsertRequest request = new ProductUpsertRequest();
        request.setCategoryId(categoryId);
        request.setProductCode(productCode);
        request.setName(name);
        request.setDescription(description);
        request.setImageUrl(imageUrl);
        request.setPrice(price);
        request.setOriginalPrice(originalPrice);
        request.setStock(0);
        request.setRecommended(recommended);
        request.setIsNew(isNew);
        request.setSort(sort);
        return request;
    }
}
