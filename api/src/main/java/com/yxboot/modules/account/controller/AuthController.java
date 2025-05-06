package com.yxboot.modules.account.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.common.exception.ApiException;
import com.yxboot.config.security.jwt.JwtUtil;
import com.yxboot.modules.account.dto.UserDTO;
import com.yxboot.modules.account.entity.User;
import com.yxboot.modules.account.enums.TenantUserRole;
import com.yxboot.modules.account.enums.UserStatus;
import com.yxboot.modules.account.service.TenantService;
import com.yxboot.modules.account.service.TenantUserService;
import com.yxboot.modules.account.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/api/auth")
@Tag(name = "认证API", description = "用户登录和认证相关的接口")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;
    private final TenantService tenantService;
    private final TenantUserService tenantUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.token-prefix:Bearer}") // 默认带空格
    private String tokenPrefix;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT令牌")
    public Result<UserDTO> login(@Valid @RequestBody LoginRequest loginRequest) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userService.getUserByEmail(email);

        if (user == null) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "邮箱或密码错误");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            return Result.error(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        if (user.getStatus() == UserStatus.CLOSED) {
            return Result.error(ResultCode.FORBIDDEN, "账号已关闭");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "邮箱或密码错误");
        }

        user.setLastLoginTime(LocalDateTime.now());
        userService.saveOrUpdate(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        String token = jwtUtil.generateToken(user.getEmail(), claims);

        // 使用LoginResultDTO封装登录结果
        UserDTO loginResult = UserDTO.builder()
                .token(tokenPrefix + " " + token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .build();

        return Result.success("登录成功", loginResult);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口，返回注册结果")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {

        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();

        User existingUser = userService.getUserByEmail(email);
        if (existingUser != null) {
            return Result.error(ResultCode.VALIDATE_FAILED, "该邮箱已被注册");
        }

        Long userId = userService.createUser(username, email, passwordEncoder.encode(password), UserStatus.ACTIVE);
        Long tenantId = tenantService.createTenant(username);
        tenantUserService.addTenantUser(tenantId, userId, TenantUserRole.OWNER);

        return Result.success("注册成功");
    }

    @Data
    public static class LoginRequest {

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class RegisterRequest {

        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        private String password;
    }
}
