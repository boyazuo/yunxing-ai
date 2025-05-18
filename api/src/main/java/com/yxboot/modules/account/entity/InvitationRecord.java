package com.yxboot.modules.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.yxboot.modules.account.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("invitation_record")
public class InvitationRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("inviter_tenant_id")
    private Long inviterTenantId;

    @TableField("inviter_user_id")
    private Long inviterUserId;

    @TableField("invitee_email")
    private String inviteeEmail;

    @TableField("invitee_role")
    private String inviteeRole;

    @TableField(value = "invite_time", fill = FieldFill.INSERT)
    private LocalDateTime inviteTime;

    private InvitationStatus status;

    private String token;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("accept_time")
    private LocalDateTime acceptTime;
}