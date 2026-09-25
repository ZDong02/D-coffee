package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CartAddItemRequest {
    @NotNull @Positive
    private Long storeId;
    @NotNull @Positive
    private Long productId;
    @Valid
    private List<OptionSelection> options = new ArrayList<>();
    private List<@Positive Long> extraIds = new ArrayList<>();

    @Getter
    @Setter
    public static class OptionSelection {
        @NotNull @Positive
        private Long optionId;
        private List<@NotNull @Positive Long> valueIds = new ArrayList<>();
    }
}
