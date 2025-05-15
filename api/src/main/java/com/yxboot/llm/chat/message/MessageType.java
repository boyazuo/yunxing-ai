package com.yxboot.llm.chat.message;

/**
 * 消息类型枚举
 * 定义了不同类型的消息，用于大模型对话
 * 
 * @author Boya
 */
public enum MessageType {
    /**
     * 系统消息
     */
    SYSTEM,

    /**
     * 用户消息
     */
    USER,

    /**
     * AI助手消息
     */
    ASSISTANT,

    /**
     * 工具执行结果消息
     */
    TOOL_EXECUTION_RESULT,

    /**
     * 自定义消息类型
     */
    CUSTOM
}
