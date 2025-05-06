package com.yxboot.modules.account.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yxboot.modules.account.enums.TenantUserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户成员表实体类
 * 
 * @author Boya
 */
@Data
@TableName("tenant_user")
@Schema(description = "租户成员信息")
public class TenantUser {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}