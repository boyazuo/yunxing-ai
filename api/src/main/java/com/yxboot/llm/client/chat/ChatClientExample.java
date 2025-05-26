package com.yxboot.llm.client.chat;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.message.SystemMessage;
import com.yxboot.llm.chat.message.UserMessage;
import com.yxboot.llm.chat.prompt.ChatOptions;
import com.yxboot.modules.ai.entity.Provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * ChatClient使用示例
 * 展示如何在业务层使用ChatClient进行AI对话
 * 
 * @author Boya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatClientExample {

    private final ChatClient chatClient;
    private final EnhancedChatClient enhancedChatClient;

    /**
     * 示例1：简单文本对话
     */
    public String simpleChat(Provider provider, String userMessage) {
        log.info("开始简单对话，用户消息: {}", userMessage);

        try {
            String response = chatClient.chat(provider, userMessage);
            log.info("AI回复: {}", response);
            return response;
        } catch (Exception e) {
            log.error("对话失败: {}", e.getMessage(), e);
            throw new RuntimeException("对话服务暂时不可用", e);
        }
    }

    /**
     * 示例2：多轮对话
     */
    public String multiTurnChat(Provider provider, String systemPrompt, String userMessage) {
        log.info("开始多轮对话，系统提示: {}, 用户消息: {}", systemPrompt, userMessage);

        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userMessage));

        try {
            String response = chatClient.chat(provider, messages);
            log.info("AI回复: {}", response);
            return response;
        } catch (Exception e) {
            log.error("多轮对话失败: {}", e.getMessage(), e);
            throw new RuntimeException("对话服务暂时不可用", e);
        }
    }

    /**
     * 示例3：带参数的对话
     */
    public String chatWithCustomOptions(Provider provider, String userMessage) {
        log.info("开始带参数对话，用户消息: {}", userMessage);

        ChatOptions options = ChatOptions.builder()
                .temperature(0.8f)
                .maxTokens(1000)
                .topP(0.9f)
                .build();

        try {
            var response = chatClient.chatWithOptions(provider, userMessage, options);
            log.info("AI回复: {}", response.getContent());
            return response.getContent();
        } catch (Exception e) {
            log.error("带参数对话失败: {}", e.getMessage(), e);
            throw new RuntimeException("对话服务暂时不可用", e);
        }
    }

    /**
     * 示例4：流式对话
     */
    public Flux<String> streamingChat(Provider provider, String userMessage) {
        log.info("开始流式对话，用户消息: {}", userMessage);

        return chatClient.streamChat(provider, userMessage)
                .doOnNext(chunk -> log.debug("收到流式响应: {}", chunk))
                .doOnComplete(() -> log.info("流式对话完成"))
                .doOnError(error -> log.error("流式对话失败: {}", error.getMessage(), error));
    }

    /**
     * 示例5：带重试的对话
     */
    public String chatWithRetry(Provider provider, String userMessage) {
        log.info("开始带重试对话，用户消息: {}", userMessage);

        try {
            String response = enhancedChatClient.chatWithRetry(provider, userMessage);
            log.info("AI回复: {}", response);
            return response;
        } catch (Exception e) {
            log.error("带重试对话失败: {}", e.getMessage(), e);
            throw new RuntimeException("对话服务在多次重试后仍然失败", e);
        }
    }

    /**
     * 示例6：异步对话
     */
    public void asyncChat(Provider provider, String userMessage) {
        log.info("开始异步对话，用户消息: {}", userMessage);

        enhancedChatClient.chatAsync(provider, userMessage)
                .subscribe(
                        response -> {
                            log.info("异步对话完成，AI回复: {}", response);
                            // 这里可以处理响应，比如保存到数据库、发送通知等
                        },
                        error -> {
                            log.error("异步对话失败: {}", error.getMessage(), error);
                            // 这里可以处理错误，比如记录错误日志、发送告警等
                        });
    }

    /**
     * 示例7：批量处理
     */
    public Flux<String> batchProcess(Provider provider, List<String> messages) {
        log.info("开始批量处理，消息数量: {}", messages.size());

        return enhancedChatClient.batchChat(provider, messages)
                .doOnNext(response -> log.debug("批量处理响应: {}", response))
                .doOnComplete(() -> log.info("批量处理完成"))
                .doOnError(error -> log.error("批量处理失败: {}", error.getMessage(), error));
    }

    /**
     * 示例8：智能客服场景
     */
    public String customerService(Provider provider, String customerQuestion, String context) {
        log.info("处理客服问题: {}", customerQuestion);

        String systemPrompt = """
                你是一个专业的客服助手，请根据以下上下文信息回答客户问题：

                上下文信息：
                %s

                请遵循以下原则：
                1. 回答要准确、专业、友好
                2. 如果无法确定答案，请诚实说明
                3. 提供具体的解决方案或建议
                4. 保持简洁明了
                """.formatted(context);

        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(customerQuestion));

        ChatOptions options = ChatOptions.builder()
                .temperature(0.3f) // 较低的温度确保回答更加准确
                .maxTokens(500)
                .build();

        try {
            var response = chatClient.chatWithOptions(provider, messages, options);
            log.info("客服回复: {}", response.getContent());
            return response.getContent();
        } catch (Exception e) {
            log.error("客服对话失败: {}", e.getMessage(), e);
            return "抱歉，系统暂时无法处理您的问题，请稍后再试或联系人工客服。";
        }
    }

    /**
     * 示例9：代码生成场景
     */
    public String generateCode(Provider provider, String requirement) {
        log.info("生成代码需求: {}", requirement);

        String systemPrompt = """
                你是一个专业的程序员助手，请根据用户需求生成高质量的代码。

                要求：
                1. 代码要规范、可读性强
                2. 添加必要的注释
                3. 考虑错误处理
                4. 遵循最佳实践
                """;

        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(requirement));

        ChatOptions options = ChatOptions.builder()
                .temperature(0.2f) // 更低的温度确保代码更加准确
                .maxTokens(2000)
                .build();

        try {
            var response = chatClient.chatWithOptions(provider, messages, options);
            log.info("代码生成完成");
            return response.getContent();
        } catch (Exception e) {
            log.error("代码生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("代码生成服务暂时不可用", e);
        }
    }

    /**
     * 示例10：文档总结场景
     */
    public Flux<String> summarizeDocument(Provider provider, String document) {
        log.info("开始文档总结，文档长度: {}", document.length());

        String systemPrompt = """
                你是一个专业的文档分析师，请对以下文档进行总结：

                要求：
                1. 提取关键信息和要点
                2. 保持逻辑清晰
                3. 突出重要内容
                4. 控制总结长度适中
                """;

        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage("请总结以下文档：\n\n" + document));

        ChatOptions options = ChatOptions.builder()
                .temperature(0.5f)
                .maxTokens(1500)
                .stream(true)
                .build();

        return chatClient.streamChatWithOptions(provider, messages, options)
                .map(response -> response.getContent())
                .doOnNext(chunk -> log.debug("总结片段: {}", chunk))
                .doOnComplete(() -> log.info("文档总结完成"))
                .doOnError(error -> log.error("文档总结失败: {}", error.getMessage(), error));
    }
}