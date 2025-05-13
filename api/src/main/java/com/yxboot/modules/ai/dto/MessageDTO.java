package com.yxboot.modules.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息DTO
 * 
 * @author Boya
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对话消息")
public class MessageDTO {

    @Schema(description = "角色(system:系统, user:用户, assistant:助手)")
    private String role;

    @Schema(description = "内容")
    private String content;
}