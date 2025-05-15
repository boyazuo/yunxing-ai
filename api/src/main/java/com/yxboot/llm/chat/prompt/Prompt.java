package com.yxboot.llm.chat.prompt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.message.UserMessage;

import lombok.Getter;

/**
 * 提示词封装
 * 用于向大模型传递消息列表和参数
 * 
 * @author Boya
 */
@Getter
public class Prompt {

    /**
     * 消息列表
     */
    private final List<Message> messages;

    /**
     * 模型参数
     */
    private final ChatOptions options;

    /**
     * 创建只包含一条用户消息的提示词
     * 
     * @param message 用户消息内容
     */
    public Prompt(String message) {
        this(new UserMessage(message));
    }

    /**
     * 创建包含一条消息的提示词
     * 
     * @param message 消息对象
     */
    public Prompt(Message message) {
        this(Collections.singletonList(message));
    }

    /**
     * 创建包含多条消息的提示词
     * 
     * @param messages 消息对象数组
     */
    public Prompt(Message... messages) {
        this(Arrays.asList(messages));
    }

    /**
     * 创建包含消息列表的提示词
     * 
     * @param messages 消息列表
     */
    public Prompt(List<Message> messages) {
        this(messages, new ChatOptions());
    }

    /**
     * 创建包含消息列表和参数的提示词
     * 
     * @param messages 消息列表
     * @param options  模型参数
     */
    public Prompt(List<Message> messages, ChatOptions options) {
        this.messages = new ArrayList<>(messages);
        this.options = options;
    }

    /**
     * 创建一个包含相同消息但不同参数的提示词
     * 
     * @param options 新的模型参数
     * @return 新的提示词对象
     */
    public Prompt withOptions(ChatOptions options) {
        return new Prompt(this.messages, options);
    }

    /**
     * 创建一个添加了新消息的提示词
     * 
     * @param message 要添加的消息
     * @return 新的提示词对象
     */
    public Prompt withMessage(Message message) {
        List<Message> newMessages = new ArrayList<>(this.messages);
        newMessages.add(message);
        return new Prompt(newMessages, this.options);
    }

    /**
     * 设置单个参数
     * 
     * @param key   参数名
     * @param value 参数值
     * @return 带有新参数的提示词
     */
    public Prompt withOption(String key, Object value) {
        return new Prompt(this.messages, this.options.withParameter(key, value));
    }
}
