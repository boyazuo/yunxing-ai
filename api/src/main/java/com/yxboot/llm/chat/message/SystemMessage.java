package com.yxboot.llm.chat.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 系统消息
 * 用于向模型传递系统级指令
 * 
 * @author Boya
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessage implements Message {

    /**
     * 消息内容
     */
    private String content;

    @Override
    public MessageType type() {
        return MessageType.SYSTEM;
    }
}