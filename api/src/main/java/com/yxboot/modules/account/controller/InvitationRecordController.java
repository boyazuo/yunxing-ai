package com.yxboot.modules.account.controller;


import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.annotation.TableField;
import com.yxboot.common.api.Result;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.account.entity.InvitationRecord;
import com.yxboot.modules.account.entity.User;
import com.yxboot.modules.account.enums.InvitationStatus;
import com.yxboot.modules.account.service.InvitationRecordService;
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
public class InvitationRecordController {

    private final InvitationRecordService invitationRecordService;

    private final EmailService emailService;


    @PostMapping
    @Operation(summary = "创建邀请", description = "创建一个邀请")
    public Result<Void> create(@RequestBody InvitationRecordRequest invitationRecordRequest,
                               @AuthenticationPrincipal SecurityUser securityUser
    ) {
        String token = UUID.randomUUID().toString();
        // 计算过期时间，30分钟后过期
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(30);
        // 创建邀请记录
        invitationRecordService.save(InvitationRecord.builder()
                .inviterTenantId(invitationRecordRequest.inviterTenantId)
                .inviterUserId(securityUser.getUserId())
                .inviteeEmail(invitationRecordRequest.inviteeEmail)
                .inviteeRole(invitationRecordRequest.inviteeRole)
                .status(InvitationStatus.PENDING)
                .token(token)
                .expireTime(expireTime)
                .build());
        // 生成邀请链接
        String url = "http://localhost:8080/v1/api/invitations/" + token + "/user";
        // 发送邮件
        emailService.sendSimpleMessage(invitationRecordRequest.inviteeEmail, "云行AI邀请链接", "点击链接完成注册：" + url);
        return Result.success("发送邀请成功");
    }

    @GetMapping("/{token}/user")
    @Operation(summary = "获取邀请用户", description = "获取邀请用户，可以判断被邀请用户是不是系统内用户")
    public Result<User> getInvitationUser(@PathVariable String token) {
        // TODO

        return null;
    }

    @Data
    public static class InvitationRecordRequest {
        private Long inviterTenantId;
        private String inviteeEmail;
        private String inviteeRole;
    }
}
