package com.yxboot.modules.account.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.account.entity.Invitation;
import com.yxboot.modules.account.entity.TenantUser;
import com.yxboot.modules.account.enums.InvitationStatus;
import com.yxboot.modules.account.service.InvitationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.yxboot.common.api.Result;
import com.yxboot.common.api.ResultCode;
import com.yxboot.common.exception.ApiException;
import com.yxboot.config.security.jwt.JwtUtil;
import com.yxboot.modules.account.dto.TenantUserDTO;
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
    private final InvitationService invitationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.token-prefix:Bearer}") // 默认带空格
    private String tokenPrefix;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT令牌")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {

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

        // 加入租户
        if (StringUtils.isNotBlank(loginRequest.getToken())) {
            Invitation invitation = invitationService
                    .lambdaQuery()
                    .eq(Invitation::getToken, loginRequest.getToken())
                    .one();
            if (invitation != null) {
                // 判断 是否已经加入
                Long count = tenantUserService
                        .lambdaQuery()
                        .eq(TenantUser::getUserId, user.getUserId())
                        .eq(TenantUser::getTenantId, invitation.getInviterTenantId())
                        .count();
                // 没有加入，进行加入操作
                if (count == 0) {
                    // 添加团队用户
                    tenantUserService.addTenantUser(invitation.getInviterTenantId(), user.getUserId(), invitation.getInviteeRole());
                    // 更新邀请状态
                    invitation.setStatus(InvitationStatus.ACCEPTED);
                    invitation.setAcceptTime(LocalDateTime.now());
                    invitationService.saveOrUpdate(invitation);
                }
            }
        }

        // 获取用户所属的租户
        List<TenantUserDTO> tenantUserDTOs = tenantService.getTenantsByUserId(user.getUserId());

        // 获取活跃租户
        TenantUserDTO activeTenantUserDTO = tenantUserDTOs.stream()
                .filter(TenantUserDTO::getIsActive)
                .findFirst()
                .orElse(null);

        if (activeTenantUserDTO == null) {
            throw new ApiException(ResultCode.VALIDATE_FAILED, "用户未激活");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        String token = jwtUtil.generateToken(user.getEmail(), claims);

        // 使用LoginResultDTO封装登录结果
        UserDTO userDTO = UserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .build();
        Map<String, Object> result = new HashMap<>();
        result.put("token", tokenPrefix + " " + token);
        result.put("user", userDTO);
        result.put("tenant", activeTenantUserDTO);

        return Result.success("登录成功" + (StringUtils.isNotBlank(loginRequest.getToken()) ? "，成功加入团队" : ""), result);
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

        // 如果存在token，需要加入token 关联的团队
        if (StringUtils.isNotBlank(registerRequest.getToken())) {
            Invitation invitation = invitationService
                    .lambdaQuery()
                    .eq(Invitation::getToken, registerRequest.getToken())
                    .one();

            if (invitation != null) {
                // 添加团队用户
                tenantUserService.addTenantUser(invitation.getInviterTenantId(), userId, invitation.getInviteeRole());
                // 更新邀请状态
                invitation.setStatus(InvitationStatus.ACCEPTED);
                invitation.setAcceptTime(LocalDateTime.now());
                invitationService.saveOrUpdate(invitation);
            }
        }
        return Result.success("注册成功" + (StringUtils.isNotBlank(registerRequest.getToken()) ? "，成功加入团队" : ""));
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前登录用户密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest,
                                       @AuthenticationPrincipal SecurityUser securityUser) {
        // 判断 新密码 和 确认密码 是否相同
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            return Result.error(ResultCode.VALIDATE_FAILED, "新密码和确认密码不一致");
        }

        // 判断 旧密码 是否正确
        String passwordInDB = securityUser.getPassword();
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), passwordInDB)) {
            return Result.error(ResultCode.VALIDATE_FAILED, "旧密码错误");
        }

        // 修改密码
        User user = userService.getById(securityUser.getUserId());
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userService.updateById(user);
        return Result.success("修改密码成功");
    }

    @Data
    public static class LoginRequest {

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        private String password;

        private String token;
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

        private String token;
    }

    /**
     * 修改密码接口请求参数
     */
    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String currentPassword;
        @NotBlank(message = "确认密码不能为空")
        private String confirmPassword;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
