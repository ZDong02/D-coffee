package com.dcoffee.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class UserRegisterRequest {
    @NotBlank(message = "请填写手机号")
    @Pattern(regexp = "^1[3-9][0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "请设置密码")
    @Size(min = 8, max = 72, message = "密码至少 8 个字符")
    private String password;

    @Size(max = 64, message = "昵称不能超过 64 个字符")
    private String nickname;

    public boolean isPasswordWithinUtf8Limit() {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
