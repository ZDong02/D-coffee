package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class OrderCreateRequest {
    @NotNull
    @Min(1)
    private Long storeId;

    @Size(max = 500)
    private String remark;
}
