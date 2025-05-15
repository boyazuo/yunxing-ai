package com.yxboot.llm.chat.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI助手消息
 * 用于表示模型生成的响应内容
 * 
 * @author Boya
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage implements Message {

    /**
     * 消息内容
     */
    private String content;

    @Override
    public MessageType type() {
        return MessageType.ASSISTANT;
    }
}