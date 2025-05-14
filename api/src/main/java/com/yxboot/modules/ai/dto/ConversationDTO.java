package com.yxboot.modules.ai.dto;

import java.time.LocalDateTime;

import com.yxboot.modules.ai.entity.Conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会话数据传输对象
 * 
 * @author Boya
 */
@Data
@Schema(description = "会话信息DTO")
public class ConversationDTO {

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 从会话实体转换为DTO
     * 
     * @param conversation 会话实体
     * @return 会话DTO
     */
    public static ConversationDTO fromConversation(Conversation conversation) {
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conversation.getConversationId());
        dto.setAppId(conversation.getAppId());
        dto.setTitle(conversation.getTitle());
        dto.setCreateTime(conversation.getCreateTime());
        dto.setUpdateTime(conversation.getUpdateTime());
        return dto;
    }
}