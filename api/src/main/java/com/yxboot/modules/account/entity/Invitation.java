package com.yxboot.modules.account.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;
import com.yxboot.modules.account.enums.InvitationStatus;
import com.yxboot.modules.account.enums.TenantUserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "invitation", onInsert = MyFlexListener.class)
public class Invitation implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    @Column("inviter_tenant_id")
    private Long inviterTenantId;

    @Column("inviter_user_id")
    private Long inviterUserId;

    @Column("invitee_email")
    private String inviteeEmail;

    @Column("invitee_role")
    private TenantUserRole inviteeRole;

    @Column("invite_time")
    private LocalDateTime inviteTime;

    private InvitationStatus status;

    private String token;

    @Column("expire_time")
    private LocalDateTime expireTime;

    @Column("accept_time")
    private LocalDateTime acceptTime;
}
