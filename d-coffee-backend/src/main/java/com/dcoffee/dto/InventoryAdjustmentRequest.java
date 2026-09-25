package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class InventoryAdjustmentRequest {
    @NotNull
    private Integer delta;
    @NotBlank
    @Size(max = 255)
    private String reason;
}
