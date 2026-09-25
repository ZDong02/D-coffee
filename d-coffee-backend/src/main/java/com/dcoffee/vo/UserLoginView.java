package com.dcoffee.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginView {
    private long id;
    private String phone;
    private String nickname;
    private String role;
    private String token;
}
