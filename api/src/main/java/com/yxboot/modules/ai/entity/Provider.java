package com.yxboot.modules.ai.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yxboot.modules.ai.enums.CommonStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模型提供商实体类
 * 
 * @author Boya
 */
@Data
@TableName("provider")
@Schema(description = "模型提供商信息")
public class Provider {

    @TableId(value = "provider_id", type = IdType.ASSIGN_ID)
    @Schema(description = "提供商ID")
    private Long providerId;

    @Schema(description = "所属租户ID")
    private Long tenantId;

    @Schema(description = "提供商名称")
    private String providerName;

    @Schema(description = "Logo")
    private String logo;

    @Schema(description = "API密钥")
    private String apiKey;

    @Schema(description = "终端地址")
    private String endpoint;

    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedTime;

    @Schema(description = "状态")
    private CommonStatus status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}