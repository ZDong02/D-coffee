package com.dcoffee.controller;

import com.dcoffee.common.Result;
import com.dcoffee.dto.AdminLoginRequest;
import com.dcoffee.service.AdminAuthService;
import com.dcoffee.vo.AdminLoginView;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Validated
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/auth/admin/login")
    public Result<AdminLoginView> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.success(adminAuthService.login(request));
    }
}
