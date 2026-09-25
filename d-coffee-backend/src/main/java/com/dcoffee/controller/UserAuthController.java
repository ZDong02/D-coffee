package com.dcoffee.controller;

import com.dcoffee.common.Result;
import com.dcoffee.dto.UserLoginRequest;
import com.dcoffee.dto.UserRegisterRequest;
import com.dcoffee.service.UserAuthService;
import com.dcoffee.vo.UserLoginView;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Validated
public class UserAuthController {
    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/user/register")
    public Result<UserLoginView> register(@Valid @RequestBody UserRegisterRequest request) {
        return Result.success(userAuthService.register(request));
    }

    @PostMapping("/user/login")
    public Result<UserLoginView> login(@Valid @RequestBody UserLoginRequest request) {
        return Result.success(userAuthService.login(request));
    }
}
