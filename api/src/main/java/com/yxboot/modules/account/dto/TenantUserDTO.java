package com.yxboot.modules.account.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yxboot.modules.account.enums.TenantPlan;
import com.yxboot.modules.account.enums.TenantStatus;
import com.yxboot.modules.account.enums.TenantUserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 租户信息及用户角色DTO
 * 
 * @author Boya
 */
@Data
@Schema(description = "租户信息及用户角色")
public class TenantUserDTO {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "订阅套餐")
    private TenantPlan plan;

    @Schema(description = "状态(active:活跃, closed:已关闭)")
    private TenantStatus status;

    @Schema(description = "用户角色")
    private TenantUserRole role;

    @Schema(description = "是否活跃租户")
    private Boolean isActive;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "成员数量")
    private Long memberCount;
}