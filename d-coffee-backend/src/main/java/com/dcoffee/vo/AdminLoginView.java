package com.dcoffee.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminLoginView {
    private long id;
    private String username;
    private String displayName;
    private String role;
    private String token;
}
