package com.yxboot.modules.ai.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型请求通用DTO
 * 
 * @author Boya
 */
@Data
@NoArgsConstructor
@Schema(description = "模型请求参数")
public class ModelRequestDTO {

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "提示内容")
    private String prompt;

    @Schema(description = "聊天消息列表")
    private List<ChatMessageDTO> messages;

    @Schema(description = "系统消息")
    private String systemMessage;

    @Schema(description = "温度值(0-2)")
    private Double temperature = 0.7;

    @Schema(description = "最大输出token")
    private Integer maxTokens;

    @Schema(description = "是否流式响应")
    private Boolean stream = false;

    @Schema(description = "top_p值")
    private Double topP = 1.0;

    @Schema(description = "频率惩罚")
    private Double frequencyPenalty = 0.0;

    @Schema(description = "存在惩罚")
    private Double presencePenalty = 0.0;

    @Schema(description = "额外参数")
    private Map<String, Object> extraParams;

    @Builder
    public ModelRequestDTO(Long appId, Long modelId, Long conversationId, String prompt,
            List<ChatMessageDTO> messages, String systemMessage,
            Double temperature, Integer maxTokens, Boolean stream,
            Double topP, Double frequencyPenalty, Double presencePenalty,
            Map<String, Object> extraParams) {
        this.appId = appId;
        this.modelId = modelId;
        this.conversationId = conversationId;
        this.prompt = prompt;
        this.messages = messages;
        this.systemMessage = systemMessage;
        this.temperature = temperature != null ? temperature : 0.7;
        this.maxTokens = maxTokens;
        this.stream = stream != null ? stream : false;
        this.topP = topP != null ? topP : 1.0;
        this.frequencyPenalty = frequencyPenalty != null ? frequencyPenalty : 0.0;
        this.presencePenalty = presencePenalty != null ? presencePenalty : 0.0;
        this.extraParams = extraParams;
    }
}