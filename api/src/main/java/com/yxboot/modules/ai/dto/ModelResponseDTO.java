package com.yxboot.modules.ai.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 模型响应通用DTO
 * 
 * @author Boya
 */
@Data
@Builder
@Schema(description = "模型响应结果")
public class ModelResponseDTO {

    @Schema(description = "响应内容")
    private String content;

    @Schema(description = "消息列表")
    private List<MessageDTO> messages;

    @Schema(description = "请求ID")
    private String requestId;

    @Schema(description = "模型名称")
    private String model;

    @Schema(description = "完成原因")
    private String finishReason;

    @Schema(description = "输入token数")
    private Integer promptTokens;

    @Schema(description = "输出token数")
    private Integer completionTokens;

    @Schema(description = "总token数")
    private Integer totalTokens;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "额外数据")
    private Map<String, Object> extraData;
}