package com.yxboot.modules.account.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yxboot.modules.account.enums.TenantPlan;
import com.yxboot.modules.account.enums.TenantStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户表实体类
 * 
 * @author Boya
 */
@Data
@TableName("tenant")
@Schema(description = "租户信息")
public class Tenant {

    @TableId(value = "tenant_id", type = IdType.ASSIGN_ID)
    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "订阅套餐")
    private TenantPlan plan;

    @Schema(description = "状态(active:活跃, closed:已关闭)")
    private TenantStatus status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}