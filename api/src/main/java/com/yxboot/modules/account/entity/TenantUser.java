package com.yxboot.modules.account.entity;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;
import com.yxboot.modules.account.enums.TenantUserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户成员表实体类
 *
 * @author Boya
 */
@Data
@Table(value = "tenant_user", onInsert = MyFlexListener.class, onUpdate = MyFlexListener.class)
@Schema(description = "租户成员信息")
public class TenantUser {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色(owner:所有者, admin:管理员, normal:普通成员)")
    private TenantUserRole role;

    @Schema(description = "是否活跃租户")
    private Boolean isActive;

    @Schema(description = "邀请人ID")
    private Long inviterId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
