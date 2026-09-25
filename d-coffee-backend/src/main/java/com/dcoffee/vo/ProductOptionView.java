package com.dcoffee.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductOptionView {
    private Long id;
    private String code;
    private String name;
    private String selectionType;
    private Boolean required;
    private Integer minSelect;
    private Integer maxSelect;
    private Integer sort;
    private List<OptionValueView> values;
}
