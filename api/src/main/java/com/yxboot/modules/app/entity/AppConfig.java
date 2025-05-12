package com.yxboot.modules.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用配置实体类
 */
@Data
@TableName("app_config")
@Schema(description = "应用配置")
public class AppConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "config_id", type = IdType.ASSIGN_ID)
    private Long configId;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private Long appId;

    /**
     * 所属租户ID
     */
    @Schema(description = "所属租户ID")
    private Long tenantId;

    /**
     * 系统提示词
     */
    @Schema(description = "系统提示词")
    private String sysPrompt;

    /**
     * AI模型配置
     */
    @Schema(description = "AI模型配置")
    private String models;

    /**
     * 变量配置
     */
    @Schema(description = "变量配置")
    private String variables;

    /**
     * 知识库配置
     */
    @Schema(description = "知识库配置")
    private String datasets;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    @TableField(fill = FieldFill.INSERT)
    private Long creatorId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者ID
     */
    @Schema(description = "更新者ID")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatorId;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}