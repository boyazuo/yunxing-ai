package com.yxboot.modules.ai.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模型请求DTO
 * 
 * @author Boya
 */
@Data
@Schema(description = "模型请求参数")
public class ChatRequestDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "消息列表")
    private List<ChatMessageDTO> messages;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "是否流式响应")
    private Boolean stream = true;
}
