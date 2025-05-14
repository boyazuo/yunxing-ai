package com.yxboot.modules.ai.dto;

import java.time.LocalDateTime;

import com.yxboot.modules.ai.entity.Message;
import com.yxboot.modules.ai.enums.MessageStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消息数据传输对象
 * 
 * @author Boya
 */
@Data
@Schema(description = "消息信息DTO")
public class MessageDTO {

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "问题")
    private String question;

    @Schema(description = "回复")
    private String answer;

    @Schema(description = "状态")
    private MessageStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 从消息实体转换为DTO
     * 
     * @param message 消息实体
     * @return 消息DTO
     */
    public static MessageDTO fromMessage(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setMessageId(message.getMessageId());
        dto.setConversationId(message.getConversationId());
        dto.setQuestion(message.getQuestion());
        dto.setAnswer(message.getAnswer());
        dto.setStatus(message.getStatus());
        dto.setCreateTime(message.getCreateTime());
        return dto;
    }
}