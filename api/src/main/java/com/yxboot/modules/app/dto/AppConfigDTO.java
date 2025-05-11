package com.yxboot.modules.app.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 应用配置数据传输对象
 */
@Data
public class AppConfigDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 所属租户ID
     */
    private Long tenantId;

    /**
     * 系统提示词
     */
    private String sysPrompt;

    /**
     * AI模型配置
     */
    private String models;

    /**
     * 变量配置
     */
    private String variables;

    /**
     * 知识库配置
     */
    private String datasets;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}