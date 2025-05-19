package com.yxboot.modules.ai.dto;

import java.util.List;

import com.yxboot.llm.chat.message.Message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模型请求DTO
 * 
 * @author Boya
 */
@Data
@Schema(description = "模型请求参数")
public class ModelRequestDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "消息列表")
    private List<Message> messages;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "是否流式响应")
    private Boolean stream = true;

    @Schema(description = "最大输出tokens")
    private Integer maxTokens;

    @Schema(description = "温度，控制随机性 (0-2)")
    private Float temperature = 0.7f;

    @Schema(description = "采样top_p (0-1)")
    private Float topP = 0.95f;
}