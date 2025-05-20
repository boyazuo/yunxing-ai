package com.yxboot.modules.account.controller;


import cn.hutool.core.lang.UUID;
import com.yxboot.common.api.Result;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.account.entity.Invitation;
import com.yxboot.modules.account.entity.Tenant;
import com.yxboot.modules.account.entity.User;
import com.yxboot.modules.account.enums.InvitationStatus;
import com.yxboot.modules.account.enums.TenantUserRole;
import com.yxboot.modules.account.service.InvitationService;
import com.yxboot.modules.account.service.TenantService;
import com.yxboot.modules.account.service.UserService;
import com.yxboot.modules.system.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 邀请记录管理 控制器
 */
@RestController
@RequestMapping("/v1/api/invitations")
@Tag(name = "邀请管理API", description = "邀请管理相关的接口")
@RequiredArgsConstructor
@Validated
public class InvitationController {

    private final InvitationService invitationService;
    private final EmailService emailService;
    private final UserService userService;
    private final TenantService tenantService;

    @PostMapping
    @Operation(summary = "创建邀请", description = "创建一个邀请")
    public Result<Void> create(@RequestBody InvitationRequest invitationRequest,
                               @AuthenticationPrincipal SecurityUser securityUser
    ) {

        if (securityUser.getUsername().equals(invitationRequest.inviteeEmail)) {
            return Result.error("不能邀请自己");
        }

        String token = UUID.randomUUID().toString();
        // 计算过期时间，30分钟后过期
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(30);
        // 创建邀请记录
        invitationService.save(Invitation.builder()
                .inviterTenantId(invitationRequest.inviterTenantId)
                .inviterUserId(securityUser.getUserId())
                .inviteeEmail(invitationRequest.inviteeEmail)
                .inviteeRole(invitationRequest.inviteeRole)
                .status(InvitationStatus.PENDING)
                .token(token)
                .expireTime(expireTime)
                .build());

        Tenant tenant = tenantService.getById(invitationRequest.inviterTenantId);

        // 生成邀请链接
        String url = "http://localhost:3000/login/?token=" + token;
        // 发送邮件
        emailService.sendSimpleMessage(invitationRequest.inviteeEmail, "云行AI邀请链接", "点击链接加入团队【" + tenant.getTenantName() + "】：" + url);
        return Result.success("发送邀请成功");
    }

    @GetMapping("/{token}")
    @Operation(summary = "获取邀请用户", description = "获取邀请用户，可以判断被邀请用户是不是系统内用户")
    public Result<InvitationResult> getInvitation(@PathVariable String token) {
        // TODO
        InvitationResult invitationResult = new InvitationResult();

        Invitation invitation = invitationService
                .lambdaQuery()
                .eq(Invitation::getToken, token)
                .one();

        LocalDateTime expireTime = invitation.getExpireTime();
        if (expireTime.isBefore(LocalDateTime.now())) {
            return Result.error("邀请链接已过期");
        }

        User user = userService
                .lambdaQuery()
                .eq(User::getEmail, invitation.getInviteeEmail())
                .one();

        if (user != null) {
            invitationResult.setUser(user);
        }

        invitationResult.setInvitation(invitation);
        return Result.success("获取信息成功", invitationResult);
    }

    @Data
    public static class InvitationRequest {
        private Long inviterTenantId;
        private String inviteeEmail;
        private TenantUserRole inviteeRole;
    }

    @Data
    public static class InvitationResult {
        private Invitation invitation;
        private User user;
    }
}
