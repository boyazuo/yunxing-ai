package com.yxboot.llm.chat.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 消息构建工具类
 * 提供便捷的消息构建方法
 * 
 * @author Boya
 */
public class MessageBuilder {

    private final List<Message> messages = new ArrayList<>();

    /**
     * 创建一个空的消息构建器
     * 
     * @return 消息构建器
     */
    public static MessageBuilder create() {
        return new MessageBuilder();
    }

    /**
     * 从现有消息列表创建一个消息构建器
     * 
     * @param messages 消息列表
     * @return 消息构建器
     */
    public static MessageBuilder fromMessages(List<Message> messages) {
        MessageBuilder builder = new MessageBuilder();
        if (messages != null) {
            builder.messages.addAll(messages);
        }
        return builder;
    }

    /**
     * 从现有消息数组创建一个消息构建器
     * 
     * @param messages 消息数组
     * @return 消息构建器
     */
    public static MessageBuilder fromMessages(Message... messages) {
        return fromMessages(Arrays.asList(messages));
    }

    /**
     * 添加系统消息
     * 
     * @param content 消息内容
     * @return 消息构建器
     */
    public MessageBuilder system(String content) {
        messages.add(new SystemMessage(content));
        return this;
    }

    /**
     * 添加用户消息
     * 
     * @param content 消息内容
     * @return 消息构建器
     */
    public MessageBuilder user(String content) {
        messages.add(new UserMessage(content));
        return this;
    }

    /**
     * 添加AI消息
     * 
     * @param content 消息内容
     * @return 消息构建器
     */
    public MessageBuilder assistant(String content) {
        messages.add(new AiMessage(content));
        return this;
    }

    /**
     * 添加自定义角色消息
     * 
     * @param role    角色名称
     * @param content 消息内容
     * @return 消息构建器
     */
    public MessageBuilder custom(String role, String content) {
        messages.add(new CustomMessage(role, content));
        return this;
    }

    /**
     * 添加工具执行结果消息
     * 
     * @param toolName 工具名称
     * @param content  执行结果
     * @param success  是否成功
     * @return 消息构建器
     */
    public MessageBuilder toolResult(String toolName, String content, boolean success) {
        messages.add(new ToolExecutionResultMessage(toolName, content, success));
        return this;
    }

    /**
     * 添加任意消息对象
     * 
     * @param message 消息对象
     * @return 消息构建器
     */
    public MessageBuilder add(Message message) {
        if (message != null) {
            messages.add(message);
        }
        return this;
    }

    /**
     * 构建消息列表
     * 
     * @return 消息列表
     */
    public List<Message> build() {
        return new ArrayList<>(messages);
    }
}