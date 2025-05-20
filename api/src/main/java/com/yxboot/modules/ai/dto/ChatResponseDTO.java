package com.yxboot.modules.ai.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 模型响应DTO
 * 
 * @author Boya
 */
@Data
@Builder
@Schema(description = "模型响应数据")
public class ChatResponseDTO {

    @Schema(description = "生成ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "生成的内容")
    private String content;

    @Schema(description = "模型名称")
    private String model;

    @Schema(description = "提供商名称")
    private String provider;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "输入token数量")
    private Integer promptTokens;

    @Schema(description = "输出token数量")
    private Integer completionTokens;

    @Schema(description = "总token数量")
    private Integer totalTokens;
}