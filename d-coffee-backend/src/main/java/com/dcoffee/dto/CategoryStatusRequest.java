package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
public class CategoryStatusRequest {
    @Pattern(regexp = "ACTIVE|DISABLED")
    private String status;
}
