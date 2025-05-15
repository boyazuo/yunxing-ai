package com.yxboot.modules.ai.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yxboot.modules.ai.enums.MessageStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消息实体类
 * 
 * @author Boya
 */
@Data
@TableName("message")
@Schema(description = "消息信息")
public class Message {

    @TableId(value = "message_id", type = IdType.ASSIGN_ID)
    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "问题")
    private String question;

    @Schema(description = "回复")
    private String answer;

    @Schema(description = "状态")
    private MessageStatus status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}