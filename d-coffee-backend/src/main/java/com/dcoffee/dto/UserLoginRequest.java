package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserLoginRequest {
    @NotBlank(message = "请填写手机号")
    @Pattern(regexp = "^1[3-9][0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "请填写密码")
    @Size(max = 128, message = "密码格式不正确")
    private String password;
}
