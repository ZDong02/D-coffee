package com.dcoffee.common;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.UUID;

/** 为日志添加简短请求标识，并将其返回给 API 调用方。 */
@Component
public class RequestIdFilter implements Filter {
    public static final String HEADER = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(HEADER, requestId);
        if (response instanceof javax.servlet.http.HttpServletResponse httpResponse) {
            httpResponse.setHeader(HEADER, requestId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(HEADER);
        }
    }
}
