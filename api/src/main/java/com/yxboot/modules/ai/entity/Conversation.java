package com.yxboot.modules.ai.entity;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.yxboot.config.mybatisflex.MyFlexListener;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会话实体类
 *
 * @author Boya
 */
@Data
@Table(value = "conversation", onInsert = MyFlexListener.class, onUpdate = MyFlexListener.class)
@Schema(description = "会话信息")
public class Conversation {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
