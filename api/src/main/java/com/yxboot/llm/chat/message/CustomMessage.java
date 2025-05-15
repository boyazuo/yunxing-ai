package com.yxboot.llm.chat.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 自定义消息
 * 用于支持特定模型的自定义消息类型
 * 
 * @author Boya
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomMessage implements Message {

    /**
     * 角色名称
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    @Override
    public MessageType type() {
        return MessageType.CUSTOM;
    }
}