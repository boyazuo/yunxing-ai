package com.yxboot.llm.chat.prompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.message.MessageBuilder;

/**
 * 提示词构建器
 * 提供流式API构建提示词
 * 
 * @author Boya
 */
public class PromptBuilder {

    private MessageBuilder messageBuilder;
    private final Map<String, Object> options = new HashMap<>();

    /**
     * 创建一个空的提示词构建器
     * 
     * @return 提示词构建器
     */
    public static PromptBuilder create() {
        PromptBuilder builder = new PromptBuilder();
        builder.messageBuilder = MessageBuilder.create();
        return builder;
    }

    /**
     * 从现有消息列表创建提示词构建器
     * 
     * @param messages 消息列表
     * @return 提示词构建器
     */
    public static PromptBuilder fromMessages(List<Message> messages) {
        PromptBuilder builder = new PromptBuilder();
        builder.messageBuilder = MessageBuilder.fromMessages(messages);
        return builder;
    }

    /**
     * 从现有消息数组创建提示词构建器
     * 
     * @param messages 消息数组
     * @return 提示词构建器
     */
    public static PromptBuilder fromMessages(Message... messages) {
        PromptBuilder builder = new PromptBuilder();
        builder.messageBuilder = MessageBuilder.fromMessages(messages);
        return builder;
    }

    /**
     * 添加系统消息
     * 
     * @param content 消息内容
     * @return 提示词构建器
     */
    public PromptBuilder system(String content) {
        messageBuilder.system(content);
        return this;
    }

    /**
     * 添加用户消息
     * 
     * @param content 消息内容
     * @return 提示词构建器
     */
    public PromptBuilder user(String content) {
        messageBuilder.user(content);
        return this;
    }

    /**
     * 添加AI消息
     * 
     * @param content 消息内容
     * @return 提示词构建器
     */
    public PromptBuilder assistant(String content) {
        messageBuilder.assistant(content);
        return this;
    }

    /**
     * 添加自定义角色消息
     * 
     * @param role    角色名称
     * @param content 消息内容
     * @return 提示词构建器
     */
    public PromptBuilder custom(String role, String content) {
        messageBuilder.custom(role, content);
        return this;
    }

    /**
     * 添加工具执行结果消息
     * 
     * @param toolName 工具名称
     * @param content  执行结果
     * @param success  是否成功
     * @return 提示词构建器
     */
    public PromptBuilder toolResult(String toolName, String content, boolean success) {
        messageBuilder.toolResult(toolName, content, success);
        return this;
    }

    /**
     * 添加任意消息对象
     * 
     * @param message 消息对象
     * @return 提示词构建器
     */
    public PromptBuilder add(Message message) {
        messageBuilder.add(message);
        return this;
    }

    /**
     * 设置单个参数
     * 
     * @param key   参数名
     * @param value 参数值
     * @return 提示词构建器
     */
    public PromptBuilder option(String key, Object value) {
        options.put(key, value);
        return this;
    }

    /**
     * 设置模型温度参数
     * 
     * @param temperature 温度值(0.0-2.0)
     * @return 提示词构建器
     */
    public PromptBuilder temperature(double temperature) {
        return option("temperature", temperature);
    }

    /**
     * 设置最大生成token数
     * 
     * @param maxTokens 最大token数
     * @return 提示词构建器
     */
    public PromptBuilder maxTokens(int maxTokens) {
        return option("maxTokens", maxTokens);
    }

    /**
     * 设置流式响应选项
     * 
     * @param stream 是否启用流式响应
     * @return 提示词构建器
     */
    public PromptBuilder stream(boolean stream) {
        return option("stream", stream);
    }

    /**
     * 构建提示词对象
     * 
     * @return 提示词对象
     */
    public Prompt build() {
        List<Message> messages = messageBuilder.build();
        ChatOptions chatOptions = new ChatOptions();
        options.forEach(chatOptions::withParameter);
        return new Prompt(messages, chatOptions);
    }
}