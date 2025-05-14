package com.yxboot.modules.ai.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 模型请求通用DTO
 * 
 * @author Boya
 */
@Data
@Builder
@Schema(description = "模型请求参数")
public class ModelRequestDTO {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "提示内容")
    private String prompt;

    @Schema(description = "聊天消息列表")
    private List<ChatMessageDTO> messages;

    @Schema(description = "系统消息")
    private String systemMessage;

    @Schema(description = "温度值(0-2)")
    @Builder.Default
    private Double temperature = 0.7;

    @Schema(description = "最大输出token")
    private Integer maxTokens;

    @Schema(description = "是否流式响应")
    @Builder.Default
    private Boolean stream = false;

    @Schema(description = "top_p值")
    @Builder.Default
    private Double topP = 1.0;

    @Schema(description = "频率惩罚")
    @Builder.Default
    private Double frequencyPenalty = 0.0;

    @Schema(description = "存在惩罚")
    @Builder.Default
    private Double presencePenalty = 0.0;

    @Schema(description = "额外参数")
    private Map<String, Object> extraParams;
}