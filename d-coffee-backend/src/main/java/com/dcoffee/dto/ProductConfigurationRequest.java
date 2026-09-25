package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductConfigurationRequest {
    private List<OptionGroup> options = new ArrayList<>();
    private List<Extra> extras = new ArrayList<>();

    @Getter
    @Setter
    public static class OptionGroup {
        private String code;
        private String name;
        private String selectionType;
        private Boolean required;
        private Integer minSelect;
        private Integer maxSelect;
        private Integer sort;
        private List<OptionValue> values = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class OptionValue {
        private String code;
        private String name;
        private BigDecimal priceDelta;
        private Integer sort;
    }

    @Getter
    @Setter
    public static class Extra {
        private String code;
        private String name;
        private BigDecimal price;
        private Integer stock;
        private Integer sort;
    }
}
