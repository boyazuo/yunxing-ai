package com.yxboot.llm.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.yxboot.llm.chat.message.AiMessage;
import com.yxboot.llm.chat.message.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应类
 * 封装大模型的响应内容
 * 
 * @author Boya
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 响应内容
     */
    private String content;

    /**
     * 响应消息对象
     */
    private AiMessage message;

    /**
     * 使用的令牌数
     */
    private TokenUsage tokenUsage;

    /**
     * 原始响应数据
     */
    private Map<String, Object> metadata;

    /**
     * 生成的附加消息列表
     */
    @Builder.Default
    private List<Message> additionalMessages = new ArrayList<>();

    /**
     * 创建一个只包含内容的响应
     * 
     * @param content 响应内容
     * @return 聊天响应对象
     */
    public static ChatResponse of(String content) {
        AiMessage message = new AiMessage(content);
        return ChatResponse.builder()
                .content(content)
                .message(message)
                .build();
    }

    /**
     * 创建一个包含内容和元数据的响应
     * 
     * @param content  响应内容
     * @param metadata 元数据
     * @return 聊天响应对象
     */
    public static ChatResponse of(String content, Map<String, Object> metadata) {
        AiMessage message = new AiMessage(content);
        return ChatResponse.builder()
                .content(content)
                .message(message)
                .metadata(metadata)
                .build();
    }

    /**
     * 获取所有生成的消息
     * 
     * @return 消息列表
     */
    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        messages.addAll(additionalMessages);
        return Collections.unmodifiableList(messages);
    }

    /**
     * 令牌使用统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {

        /**
         * 输入令牌数
         */
        private int inputTokens;

        /**
         * 输出令牌数
         */
        private int outputTokens;

        /**
         * 总令牌数
         */
        private int totalTokens;

        /**
         * 创建一个令牌使用统计对象
         * 
         * @param inputTokens  输入令牌数
         * @param outputTokens 输出令牌数
         * @return 令牌使用统计对象
         */
        public static TokenUsage of(int inputTokens, int outputTokens) {
            return TokenUsage.builder()
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .totalTokens(inputTokens + outputTokens)
                    .build();
        }
    }
}