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

    /**
     * 使用示例：使用Flux进行流式处理
     * 
     * <pre>{@code
     * // 1. 直接处理聊天响应流
     * model.stream("你好，请介绍一下自己")
     *     .doOnNext(response -> System.out.print(response.getContent()))
     *     .subscribe();
     * 
     * // 2. 处理内容字符流
     * model.streamContent("你好，请介绍一下自己")
     *     .doOnNext(System.out::print)
     *     .subscribe();
     * 
     * // 3. 处理消息流
     * model.streamMessages("你能给我讲个笑话吗")
     *     .doOnNext(message -> {
     *         System.out.println("\n当前生成的内容: " + message.getContent());
     *     })
     *     .subscribe();
     * 
     * // 4. 获取完整结果
     * model.streamToResponse("帮我写一首诗")
     *     .doOnSuccess(response -> {
     *         System.out.println("\n完整内容: " + response.getContent());
     *     })
     *     .subscribe();
     * 
     * // 5. 使用WebFlux在Controller中返回流
     * &#64;GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
     * public Flux<ChatResponse> streamChat(@RequestParam String message) {
     *     return model.stream(message);
     * }
     * }</pre>
     */
    default void exampleUsage() {
        // 这是一个示例方法，实际使用时不需要调用此方法
    }
}