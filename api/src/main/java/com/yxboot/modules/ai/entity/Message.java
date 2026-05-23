package com.yxboot.modules.ai.entity;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;
import com.yxboot.modules.ai.enums.MessageStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消息实体类
 *
 * @author Boya
 */
@Data
@Table(value = "message", onInsert = MyFlexListener.class)
@Schema(description = "消息信息")
public class Message {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
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
    private LocalDateTime createTime;
}
