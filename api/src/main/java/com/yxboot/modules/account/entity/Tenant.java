package com.yxboot.modules.account.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;
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
@Table(value = "tenant", onInsert = MyFlexListener.class, onUpdate = MyFlexListener.class)
@Schema(description = "租户信息")
public class Tenant {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "订阅套餐")
    private TenantPlan plan;

    @Schema(description = "状态(active:活跃, closed:已关闭)")
    private TenantStatus status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
