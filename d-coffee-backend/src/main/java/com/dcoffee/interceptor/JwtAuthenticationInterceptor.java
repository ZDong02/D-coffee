package com.dcoffee.interceptor;

import com.dcoffee.utils.AuthContext;
import com.dcoffee.utils.AuthUser;
import com.dcoffee.utils.JwtTokenUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    private static final String AUTHORIZATION = "Authorization";
    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationInterceptor(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader(AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return false;
        }
        try {
            AuthUser user = jwtTokenUtil.parseToken(authorization.substring("Bearer ".length()).trim());
            AuthContext.set(user);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            writeUnauthorized(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
        } catch (java.io.IOException ignored) {
            // 响应由容器管理；若连接已关闭，则无需继续处理。
        }
    }
}
