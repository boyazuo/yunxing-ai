package com.yxboot.modules.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用配置实体类
 */
@Data
@Table(value = "app_config", onInsert = MyFlexListener.class, onUpdate = MyFlexListener.class)
@Schema(description = "应用配置")
public class AppConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long configId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "所属租户ID")
    private Long tenantId;

    @Schema(description = "系统提示词")
    private String sysPrompt;

    @Schema(description = "AI模型配置")
    private String models;

    @Schema(description = "变量配置")
    private String variables;

    @Schema(description = "知识库配置")
    private String datasets;

    @Schema(description = "创建者ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者ID")
    private Long updatorId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
