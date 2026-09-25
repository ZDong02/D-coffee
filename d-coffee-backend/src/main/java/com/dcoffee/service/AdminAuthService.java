package com.dcoffee.service;

import com.dcoffee.dto.AdminLoginRequest;
import com.dcoffee.exception.BizException;
import com.dcoffee.exception.ErrorCode;
import com.dcoffee.mapper.AdminMapper;
import com.dcoffee.utils.JwtTokenUtil;
import com.dcoffee.utils.PasswordUtil;
import com.dcoffee.vo.AdminLoginView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {
    private final AdminMapper adminMapper;
    private final PasswordUtil passwordUtil;
    private final JwtTokenUtil jwtTokenUtil;

    public AdminAuthService(AdminMapper adminMapper, PasswordUtil passwordUtil, JwtTokenUtil jwtTokenUtil) {
        this.adminMapper = adminMapper;
        this.passwordUtil = passwordUtil;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Transactional
    public AdminLoginView login(AdminLoginRequest request) {
        AdminMapper.AdminLoginRecord admin = adminMapper.findActiveAdmin(request.getUsername().trim());
        if (admin == null || !"ADMIN".equals(admin.getRole())
                || !passwordUtil.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        adminMapper.updateLastLogin(admin.getId());
        return new AdminLoginView(admin.getId(), admin.getUsername(), admin.getDisplayName(), admin.getRole(),
                jwtTokenUtil.createToken(admin.getId(), admin.getRole()));
    }
}
