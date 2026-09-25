package com.dcoffee.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryView {
    private Long id;
    private String code;
    private String name;
    private String iconUrl;
    private Integer sort;
    private String status;
}
