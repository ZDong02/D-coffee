package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CategoryUpsertRequest {
    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_-]{1,48}")
    private String code;

    @NotBlank
    @Size(max = 64)
    private String name;

    @Size(max = 500)
    private String iconUrl;

    @Min(-9999)
    @Max(9999)
    private Integer sort = 0;
}
