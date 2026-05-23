package com.yxboot.modules.ai.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.yxboot.ai.registry.ChatModelRegistry;
import com.yxboot.ai.support.SpringAiMessageConverter;
import com.yxboot.modules.ai.dto.ChatRequestDTO;
import com.yxboot.modules.ai.dto.ChatResponseDTO;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.enums.MessageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 模型调用服务（基于 Spring AI ChatModel）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageService messageService;
    private final ChatModelRegistry chatModelRegistry;

    public ChatResponseDTO chatCompletion(Provider provider, ChatRequestDTO request) throws IOException {
        log.info("开始处理聊天请求，提供商：{}，模型：{}", provider.getProviderName(), request.getModelName());
        try {
            var chatModel = chatModelRegistry.getOrCreate(provider);
            Prompt prompt = buildPrompt(request);
            ChatResponse response = chatModel.call(prompt);
            return buildResponseDTO(response, provider, request.getModelName());
        } catch (Exception e) {
            log.error("聊天请求处理异常", e);
            throw new IOException("模型调用失败：" + e.getMessage(), e);
        }
    }

    public void streamingChatCompletion(Provider provider, ChatRequestDTO request,
            SseEmitter emitter, Long messageId) throws IOException {
        try {
            var chatModel = chatModelRegistry.getOrCreate(provider);
            Prompt prompt = buildPrompt(request);
            StringBuilder fullResponseBuilder = new StringBuilder();

            emitter.send(SseEmitter.event()
                    .name("metadata")
                    .data(Map.of(
                            "conversationId", request.getConversationId(),
                            "messageId", messageId)));

            Flux<ChatResponse> responseStream = chatModel.stream(prompt);

            responseStream
                    .doOnNext(response -> {
                        try {
                            String chunk = extractContent(response);
                            fullResponseBuilder.append(chunk);
                            if (chunk != null && !chunk.trim().isEmpty()) {
                                emitter.send(SseEmitter.event().data(Map.of("chunk", chunk)));
                            }
                        } catch (IOException e) {
                            log.error("发送流式数据异常", e);
                        }
                    })
                    .doOnComplete(() -> {
                        try {
                            if (messageId != null) {
                                messageService.updateMessageAnswer(
                                        messageId,
                                        fullResponseBuilder.toString(),
                                        MessageStatus.COMPLETED);
                            }
                            emitter.send(SseEmitter.event().name("end").data(""));
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("关闭SSE发射器或更新消息状态异常", e);
                        }
                    })
                    .doOnError(error -> {
                        try {
                            if (messageId != null) {
                                messageService.updateMessageAnswer(
                                        messageId,
                                        "处理失败: " + error.getMessage(),
                                        MessageStatus.FAILED);
                            }
                            log.error("流式处理发生错误", error);
                            emitter.completeWithError(error);
                        } catch (Exception e) {
                            log.error("关闭SSE发射器异常", e);
                        }
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("流式聊天请求处理异常", e);
            if (messageId != null) {
                messageService.updateMessageAnswer(
                        messageId,
                        "处理失败: " + e.getMessage(),
                        MessageStatus.FAILED);
            }
            emitter.completeWithError(e);
        }
    }

    private Prompt buildPrompt(ChatRequestDTO request) {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            messages.addAll(SpringAiMessageConverter.toSpringAiMessages(request.getMessages()));
        }
        if (StringUtils.hasText(request.getSystemPrompt())) {
            messages.add(0, new org.springframework.ai.chat.messages.SystemMessage(request.getSystemPrompt()));
        }
        messages.add(new UserMessage(request.getPrompt()));

        ZhiPuAiChatOptions.Builder optionsBuilder = ZhiPuAiChatOptions.builder();
        if (StringUtils.hasText(request.getModelName())) {
            optionsBuilder.model(request.getModelName());
        }
        if (request.getTemperature() != null) {
            optionsBuilder.temperature(request.getTemperature().doubleValue());
        }
        if (request.getTopP() != null) {
            optionsBuilder.topP(request.getTopP().doubleValue());
        }
        if (request.getMaxTokens() != null) {
            optionsBuilder.maxTokens(request.getMaxTokens());
        }

        return new Prompt(messages, optionsBuilder.build());
    }

    private String extractContent(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        return response.getResult().getOutput().getText() != null ? response.getResult().getOutput().getText() : "";
    }

    private ChatResponseDTO buildResponseDTO(ChatResponse response, Provider provider, String modelName) {
        int promptTokens = 0;
        int completionTokens = 0;
        int totalTokens = 0;
        if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
            var usage = response.getMetadata().getUsage();
            promptTokens = (int) usage.getPromptTokens();
            completionTokens = (int) usage.getCompletionTokens();
            totalTokens = (int) usage.getTotalTokens();
        }
        return ChatResponseDTO.builder()
                .content(extractContent(response))
                .model(modelName)
                .provider(provider.getProviderName())
                .createTime(LocalDateTime.now())
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .build();
    }
}
