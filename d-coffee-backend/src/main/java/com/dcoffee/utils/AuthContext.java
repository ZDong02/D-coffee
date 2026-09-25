package com.dcoffee.utils;

/** 保存当前请求的身份信息；拦截器会在每次请求结束后清理。 */
public final class AuthContext {
    private static final ThreadLocal<AuthUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        CURRENT.set(user);
    }

    public static AuthUser getRequired() {
        AuthUser user = CURRENT.get();
        if (user == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return user;
    }

    public static AuthUser getNullable() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
