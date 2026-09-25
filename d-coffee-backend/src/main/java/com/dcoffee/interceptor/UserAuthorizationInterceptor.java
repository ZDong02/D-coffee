package com.dcoffee.interceptor;

import com.dcoffee.utils.AuthContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserAuthorizationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (AuthContext.getNullable() == null || !"USER".equals(AuthContext.getNullable().getRole())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":403,\"message\":\"仅限用户账号访问\",\"data\":null}");
            } catch (java.io.IOException ignored) {
                // 响应由容器管理；若连接已关闭，则无需继续处理。
            }
            return false;
        }
        return true;
    }
}
