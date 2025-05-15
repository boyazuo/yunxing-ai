package com.yxboot.llm.chat.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果消息
 * 用于向模型传递工具执行的结果
 * 
 * @author Boya
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionResultMessage implements Message {

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具执行结果
     */
    private String content;

    /**
     * 执行状态
     * true: 成功, false: 失败
     */
    private boolean success;

    @Override
    public MessageType type() {
        return MessageType.TOOL_EXECUTION_RESULT;
    }
}