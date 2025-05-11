package com.yxboot.modules.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 应用配置请求对象
 */
@Data
public class AppConfigRequest {

    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    private Long appId;

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
}