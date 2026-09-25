package com.dcoffee.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import com.dcoffee.interceptor.AdminAuthorizationInterceptor;
import com.dcoffee.interceptor.JwtAuthenticationInterceptor;
import com.dcoffee.interceptor.UserAuthorizationInterceptor;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.dcoffee.controller", "com.dcoffee.exception"})
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Autowired
    private AdminAuthorizationInterceptor adminAuthorizationInterceptor;

    @Autowired
    private UserAuthorizationInterceptor userAuthorizationInterceptor;

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health", "/user/login", "/user/register", "/auth/admin/login", "/categories", "/products/**", "/stores", "/error");
        registry.addInterceptor(adminAuthorizationInterceptor)
                .addPathPatterns("/admin/**");
        registry.addInterceptor(userAuthorizationInterceptor)
                .addPathPatterns("/user/cart/**", "/user/orders/**");
    }
}
