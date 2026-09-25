package com.dcoffee.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreView {
    private Long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String openTime;
    private String closeTime;
    private String status;
}
