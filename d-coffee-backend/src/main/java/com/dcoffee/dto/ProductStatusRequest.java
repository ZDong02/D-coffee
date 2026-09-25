package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class ProductStatusRequest {
    @NotBlank
    @Pattern(regexp = "DRAFT|ON_SALE|OFF_SALE")
    private String status;
}
