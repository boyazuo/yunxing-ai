package com.yxboot.modules.ai.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.yxboot.ai.config.AiProperties;
import com.yxboot.ai.rag.RagPromptResult;
import com.yxboot.ai.service.RagChatPromptService;
import com.yxboot.ai.support.SpringAiMessageConverter;
import com.yxboot.modules.ai.dto.ChatRequestDTO;
import com.yxboot.modules.ai.dto.ChatResponseDTO;
import com.yxboot.modules.ai.entity.Conversation;
import com.yxboot.modules.ai.entity.Message;
import com.yxboot.modules.ai.enums.ChatStreamPhase;
import com.yxboot.modules.ai.enums.MessageStatus;
import com.yxboot.modules.app.entity.AppConfig;
import com.yxboot.modules.app.service.AppConfigService;
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
    private final ConversationService conversationService;
    private final AppConfigService appConfigService;
    private final RagChatPromptService ragChatPromptService;
    private final ChatModel chatModel;
    private final AiProperties aiProperties;

    public ChatResponseDTO chatCompletion(ChatRequestDTO request) throws IOException {
        AiProperties.ChatConfig chatConfig = aiProperties.getChat();
        log.info("开始处理聊天请求，模型：{}", chatConfig.getModel());
        try {
            Prompt prompt = buildPrompt(request);
            ChatResponse response = chatModel.call(prompt);
            return buildResponseDTO(response, chatConfig.getModel());
        } catch (Exception e) {
            log.error("聊天请求处理异常", e);
            throw new IOException("模型调用失败：" + e.getMessage(), e);
        }
    }

    /**
     * 异步编排流式聊天：先建立 SSE 连接，再按阶段推送 status 并流式输出。
     */
    public void streamChat(Long userId, ChatRequestDTO request, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                sendStatus(emitter, ChatStreamPhase.UNDERSTANDING);

                Long conversationId = resolveConversation(userId, request);
                request.setConversationId(conversationId);

                Message message = messageService.createMessage(userId, request.getAppId(), conversationId, request.getPrompt());

                sendMetadata(emitter, conversationId, message.getMessageId());

                AppConfig appConfig = appConfigService.getByAppId(request.getAppId());
                if (ragChatPromptService.hasActiveDatasets(appConfig)) {
                    sendStatus(emitter, ChatStreamPhase.RETRIEVING);
                }

                RagPromptResult ragPrompt = ragChatPromptService.build(request.getPrompt(), appConfig);

                if (ragPrompt.getDirectResponse() != null) {
                    streamDirectResponse(emitter, message.getMessageId(), ragPrompt.getDirectResponse());
                    return;
                }

                applyRagPrompt(request, ragPrompt);
                sendStatus(emitter, ChatStreamPhase.GENERATING);
                streamingChatCompletion(request, emitter, message.getMessageId());
            } catch (Exception e) {
                log.error("流式聊天编排异常", e);
                emitter.completeWithError(e);
            }
        });
    }

    public void streamingChatCompletion(ChatRequestDTO request, SseEmitter emitter, Long messageId)
            throws IOException {
        try {
            Prompt prompt = buildPrompt(request);
            StringBuilder fullResponseBuilder = new StringBuilder();

            Flux<ChatResponse> responseStream = chatModel.stream(prompt);

            responseStream
                    .doOnNext(response -> {
                        try {
                            String chunk = extractContent(response);
                            fullResponseBuilder.append(chunk);
                            // 保留仅含换行/空白的 chunk，Markdown 块级语法依赖行首换行符
                            if (chunk != null && !chunk.isEmpty()) {
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

    private void streamDirectResponse(SseEmitter emitter, Long messageId, String content) throws IOException {
        messageService.updateMessageAnswer(messageId, content, MessageStatus.COMPLETED);
        emitter.send(SseEmitter.event().data(Map.of("chunk", content)));
        emitter.send(SseEmitter.event().name("end").data(""));
        emitter.complete();
    }

    private void sendStatus(SseEmitter emitter, ChatStreamPhase phase) throws IOException {
        emitter.send(SseEmitter.event().name("status").data(Map.of("phase", phase.getValue())));
    }

    private void sendMetadata(SseEmitter emitter, Long conversationId, Long messageId) throws IOException {
        emitter.send(SseEmitter.event()
                .name("metadata")
                .data(Map.of("conversationId", conversationId, "messageId", messageId)));
    }

    private void applyRagPrompt(ChatRequestDTO request, RagPromptResult ragPrompt) {
        request.setPrompt(ragPrompt.getUserPrompt());
        request.setSystemPrompt(ragPrompt.getSystemPrompt());
    }

    private Long resolveConversation(Long userId, ChatRequestDTO request) {
        if (request.getConversationId() != null) {
            Long conversationId = request.getConversationId();
            Conversation conversation = conversationService.getById(conversationId);
            if (conversation != null) {
                conversation.setUpdateTime(LocalDateTime.now());
                conversationService.updateById(conversation);
                return conversationId;
            }
        }

        String title = request.getPrompt();
        if (title.length() > 50) {
            title = title.substring(0, 47) + "...";
        }

        Conversation conversation = conversationService.createConversation(userId, request.getAppId(), title);
        return conversation.getConversationId();
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

        AiProperties.ChatConfig chatConfig = aiProperties.getChat();
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model(chatConfig.getModel())
                .temperature(chatConfig.getTemperature())
                .topP(chatConfig.getTopP())
                .maxTokens(chatConfig.getMaxTokens())
                .build();

        return new Prompt(messages, options);
    }

    private String extractContent(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        return response.getResult().getOutput().getText() != null ? response.getResult().getOutput().getText() : "";
    }

    private ChatResponseDTO buildResponseDTO(ChatResponse response, String modelName) {
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
                .provider(aiProperties.getChat().getProvider())
                .createTime(LocalDateTime.now())
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .build();
    }
}
