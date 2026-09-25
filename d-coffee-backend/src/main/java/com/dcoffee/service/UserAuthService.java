package com.dcoffee.service;

import com.dcoffee.dto.UserLoginRequest;
import com.dcoffee.dto.UserRegisterRequest;
import com.dcoffee.exception.BizException;
import com.dcoffee.exception.ErrorCode;
import com.dcoffee.mapper.UserAuthMapper;
import com.dcoffee.utils.JwtTokenUtil;
import com.dcoffee.utils.PasswordUtil;
import com.dcoffee.vo.UserLoginView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAuthService {
    private static final String USER_ROLE = "USER";

    private final UserAuthMapper userAuthMapper;
    private final PasswordUtil passwordUtil;
    private final JwtTokenUtil jwtTokenUtil;

    public UserAuthService(UserAuthMapper userAuthMapper, PasswordUtil passwordUtil, JwtTokenUtil jwtTokenUtil) {
        this.userAuthMapper = userAuthMapper;
        this.passwordUtil = passwordUtil;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Transactional
    public UserLoginView register(UserRegisterRequest request) {
        if (!request.isPasswordWithinUtf8Limit()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "密码 UTF-8 编码不能超过 72 字节");
        }
        String phone = request.getPhone().trim();
        if (userAuthMapper.findByPhone(phone) != null) {
            throw new BizException(ErrorCode.CONFLICT, "该手机号已注册");
        }
        String nickname = request.getNickname() == null || request.getNickname().isBlank()
                ? "咖啡用户" : request.getNickname().trim();
        userAuthMapper.insertUser(phone, passwordUtil.encode(request.getPassword()), nickname);
        UserAuthMapper.UserRecord user = userAuthMapper.findByPhone(phone);
        if (user == null) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "账号创建失败，请稍后重试");
        }
        return toView(user);
    }

    @Transactional
    public UserLoginView login(UserLoginRequest request) {
        UserAuthMapper.UserRecord user = userAuthMapper.findByPhone(request.getPhone().trim());
        if (user == null || !"ACTIVE".equals(user.getStatus())
                || !passwordUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "手机号或密码错误");
        }
        return toView(user);
    }

    private UserLoginView toView(UserAuthMapper.UserRecord user) {
        return new UserLoginView(user.getId(), user.getPhone(), user.getNickname(), USER_ROLE,
                jwtTokenUtil.createToken(user.getId(), USER_ROLE));
    }
}
