package com.yxboot.llm.chat;

import java.util.Arrays;
import java.util.List;

import com.yxboot.llm.chat.message.AiMessage;
import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.message.UserMessage;
import com.yxboot.llm.chat.prompt.ChatOptions;
import com.yxboot.llm.chat.prompt.Prompt;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 流式聊天模型接口
 * 基于Reactor Flux实现流式处理
 * 
 * @author Boya
 */
public interface StreamingChatModel {

    /**
     * 获取提供商信息
     * 
     * @return 提供商信息
     */
    ModelProvider getProvider();

    /**
     * 流式调用，发送单条用户消息并返回响应流
     * 
     * @param message 用户消息
     * @return 聊天响应流
     */
    default Flux<ChatResponse> stream(String message) {
        return stream(new Prompt(new UserMessage(message)));
    }

    /**
     * 流式调用，发送消息数组并返回响应流
     * 
     * @param messages 消息数组
     * @return 聊天响应流
     */
    default Flux<ChatResponse> stream(Message... messages) {
        return stream(new Prompt(Arrays.asList(messages)));
    }

    /**
     * 流式调用，发送消息列表并返回响应流
     * 
     * @param messages 消息列表
     * @return 聊天响应流
     */
    default Flux<ChatResponse> stream(List<Message> messages) {
        return stream(new Prompt(messages));
    }

    /**
     * 流式调用，发送提示词并返回聊天响应流
     * 该方法是接口中唯一需要实现的方法
     * 
     * @param prompt 提示词
     * @return 聊天响应流
     */
    Flux<ChatResponse> stream(Prompt prompt);

    /**
     * 获取字符流而非ChatResponse流
     * 
     * @param prompt 提示词
     * @return 字符串流
     */
    default Flux<String> streamContent(Prompt prompt) {
        return stream(prompt)
                .map(ChatResponse::getContent);
    }

    /**
     * 流式调用，发送提示词并返回AI消息流
     * 
     * @param prompt 提示词
     * @return AI消息流
     */
    default Flux<AiMessage> streamMessages(Prompt prompt) {
        return stream(prompt)
                .map(ChatResponse::getMessage);
    }

    /**
     * 流式调用，发送提示词并在流完成时返回完整响应
     * 
     * @param prompt 提示词
     * @return 聊天响应
     */
    default Mono<ChatResponse> streamToResponse(Prompt prompt) {
        return stream(prompt)
                .last();
    }

    /**
     * 获取默认选项
     * 
     * @return 默认流式选项
     */
    default ChatOptions getDefaultOptions() {
        return ChatOptions.builder()
                .stream(true)
                .build();
    }
}