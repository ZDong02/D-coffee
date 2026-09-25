package com.dcoffee.utils;

import lombok.Getter;

@Getter
public final class AuthUser {
    private final long id;
    private final String role;

    public AuthUser(long id, String role) {
        this.id = id;
        this.role = role;
    }
}
