package com.yxboot.modules.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 对话消息 DTO（用于 API 请求中的历史消息）
 */
@Data
@Schema(description = "对话消息")
public class ChatMessageDTO {

    @Schema(description = "角色: system, user, assistant")
    private String role;

    @Schema(description = "消息内容")
    private String content;
}
