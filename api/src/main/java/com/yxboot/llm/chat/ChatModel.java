package com.yxboot.llm.chat;

import java.util.Arrays;
import java.util.List;

import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.message.UserMessage;
import com.yxboot.llm.chat.prompt.ChatOptions;
import com.yxboot.llm.chat.prompt.Prompt;

/**
 * 聊天模型接口
 * 基于Spring AI的ChatModel设计思想，提供统一的聊天模型接口
 * 继承StreamingChatModel，提供流式和非流式统一接口
 * 
 * @author Boya
 */
public interface ChatModel extends StreamingChatModel {

    /**
     * 获取提供商信息
     * 
     * @return 提供商信息
     */
    ModelProvider getProvider();

    /**
     * 设置API密钥
     * 
     * @param apiKey API密钥
     * @return 当前模型实例
     */
    ChatModel withApiKey(String apiKey);

    /**
     * 简化调用，仅发送单条用户消息
     * 
     * @param message 用户消息内容
     * @return 模型响应文本
     */
    default String call(String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        ChatResponse response = call(prompt);
        return response.getContent();
    }

    /**
     * 发送多条消息
     * 
     * @param messages 消息对象列表
     * @return 模型响应文本
     */
    default String call(Message... messages) {
        Prompt prompt = new Prompt(Arrays.asList(messages));
        ChatResponse response = call(prompt);
        return response.getContent();
    }

    /**
     * 发送消息列表
     * 
     * @param messages 消息对象列表
     * @return 模型响应文本
     */
    default String call(List<Message> messages) {
        Prompt prompt = new Prompt(messages);
        ChatResponse response = call(prompt);
        return response.getContent();
    }

    /**
     * 发送提示词，获取响应
     * 
     * @param prompt 提示词对象
     * @return 聊天响应对象
     */
    default ChatResponse call(Prompt prompt) {
        // 使用流式调用的结果，阻塞获取最终结果
        return streamToResponse(prompt).block();
    }

    /**
     * 获取默认选项
     * 此方法重写StreamingChatModel的同名方法
     * 
     * @return 默认选项
     */
    @Override
    default ChatOptions getDefaultOptions() {
        return ChatOptions.builder().build();
    }
}