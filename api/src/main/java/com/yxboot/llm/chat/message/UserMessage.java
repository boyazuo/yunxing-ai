package com.yxboot.llm.chat.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户消息
 * 用于向模型传递用户输入的内容
 * 
 * @author Boya
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage implements Message {

    /**
     * 消息内容
     */
    private String content;

    @Override
    public MessageType type() {
        return MessageType.USER;
    }
}