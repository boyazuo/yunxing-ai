package com.yxboot.modules.ai.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yxboot.common.api.Result;
import com.yxboot.common.exception.ApiException;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.ai.dto.ModelRequestDTO;
import com.yxboot.modules.ai.dto.ModelResponseDTO;
import com.yxboot.modules.ai.entity.Conversation;
import com.yxboot.modules.ai.entity.Message;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.enums.MessageStatus;
import com.yxboot.modules.ai.service.AiService;
import com.yxboot.modules.ai.service.ConversationService;
import com.yxboot.modules.ai.service.MessageService;
import com.yxboot.modules.ai.service.ModelService;
import com.yxboot.modules.ai.service.ProviderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 模型调用控制器
 * 
 * @author Boya
 */
@RestController
@RequestMapping("/v1/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI模型调用接口", description = "提供AI模型调用的相关接口")
public class AiController {

    private final AiService aiService;
    private final ModelService modelService;
    private final ProviderService providerService;
    private final ConversationService conversationService;
    private final MessageService messageService;

    /**
     * 创建聊天完成
     * 
     * @param request      请求参数
     * @param securityUser 当前用户
     * @return 响应结果
     * @throws IOException 请求异常
     */
    @PostMapping("/chat")
    @Operation(summary = "创建聊天完成", description = "使用指定的模型创建聊天完成")
    public Result<ModelResponseDTO> createChatCompletion(
            @RequestBody ModelRequestDTO request,
            @AuthenticationPrincipal SecurityUser securityUser) throws IOException {

        // 验证必要参数
        if (request.getModelId() == null) {
            throw new ApiException("模型ID不能为空");
        }

        // 获取模型和提供商
        Model model = modelService.getById(request.getModelId());
        if (model == null) {
            throw new ApiException("模型不存在");
        }
        Provider provider = providerService.getById(model.getProviderId());
        if (provider == null) {
            throw new ApiException("模型提供商不存在");
        }

        // 获取问题内容
        String question = extractQuestionContent(request);

        // 处理会话和消息
        Long conversationId = request.getConversationId();
        Long userId = securityUser.getUserId();
        Long tenantId = request.getTenantId();
        Long appId = request.getAppId();

        // 创建或更新会话
        if (conversationId == null) {
            // 创建新会话
            String title = generateConversationTitle(question);
            Conversation conversation = conversationService.createConversation(tenantId, userId, appId, title);
            conversationId = conversation.getConversationId();
        } else {
            // 更新已有会话
            Conversation conversation = conversationService.getById(conversationId);
            if (conversation == null) {
                throw new ApiException("会话不存在");
            }
            // 更新会话时间
            conversation.setUpdateTime(LocalDateTime.now());
            conversationService.updateById(conversation);
        }

        // 创建消息记录
        Message message = messageService.createMessage(tenantId, userId, appId, conversationId, question);

        // 调用模型接口
        ModelResponseDTO response = aiService.chatCompletion(provider, model, request);

        // 更新消息回复
        String answer = response.getContent();
        messageService.updateMessageAnswer(message.getMessageId(), answer, MessageStatus.COMPLETED);

        return Result.success("模型调用成功", response);
    }

    /**
     * 创建流式聊天完成
     * 
     * @param request      请求参数
     * @param securityUser 当前用户
     * @return 流式响应
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "创建流式聊天完成", description = "使用指定的模型创建流式聊天完成，使用SSE进行响应")
    public SseEmitter createStreamingChatCompletion(
            @RequestBody ModelRequestDTO request,
            @AuthenticationPrincipal SecurityUser securityUser) {

        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 在单独的线程中处理流式响应
        new Thread(() -> {
            try {
                // 验证必要参数
                if (request.getModelId() == null) {
                    throw new ApiException("模型ID不能为空");
                }

                // 获取模型和提供商
                Model model = modelService.getById(request.getModelId());
                if (model == null) {
                    throw new ApiException("模型不存在");
                }
                Provider provider = providerService.getById(model.getProviderId());
                if (provider == null) {
                    throw new ApiException("模型提供商不存在");
                }

                // 获取问题内容
                String question = extractQuestionContent(request);

                // 处理会话和消息
                Long conversationId = request.getConversationId();
                Long userId = securityUser.getUserId();
                Long tenantId = request.getTenantId();
                Long appId = request.getAppId();

                // 创建或更新会话
                if (conversationId == null) {
                    // 创建新会话
                    String title = generateConversationTitle(question);
                    Conversation conversation = conversationService.createConversation(tenantId, userId, appId, title);
                    conversationId = conversation.getConversationId();
                } else {
                    // 更新已有会话
                    Conversation conversation = conversationService.getById(conversationId);
                    if (conversation == null) {
                        throw new ApiException("会话不存在");
                    }
                    // 更新会话时间
                    conversation.setUpdateTime(LocalDateTime.now());
                    conversationService.updateById(conversation);
                }

                // 创建消息记录（初始状态为处理中）
                Message message = messageService.createMessage(tenantId, userId, appId, conversationId, question);

                // 创建消息内容收集器（用于收集流式响应内容）
                StringBuilder contentCollector = new StringBuilder();

                // 设置流式响应完成回调
                emitter.onCompletion(() -> {
                    // 更新消息状态为已完成
                    messageService.updateMessageAnswer(message.getMessageId(), contentCollector.toString(),
                            MessageStatus.COMPLETED);
                });

                // 设置流式响应错误回调
                emitter.onError(e -> {
                    // 更新消息状态为失败
                    messageService.updateMessageAnswer(message.getMessageId(), e.getMessage(), MessageStatus.FAILED);
                });

                // 设置流式响应标志
                request.setStream(true);

                // 重写SSEStreamHandler实现以收集内容（匿名内部类）
                class ContentCollectingStreamHandler implements com.yxboot.modules.ai.provider.SSEStreamHandler {
                    private final com.yxboot.modules.ai.provider.SSEStreamHandler delegate;

                    public ContentCollectingStreamHandler(com.yxboot.modules.ai.provider.SSEStreamHandler delegate) {
                        this.delegate = delegate;
                    }

                    @Override
                    public void handle(SseEmitter emitter) {
                        try {
                            delegate.handle(new SseEmitter(300000L) {
                                @Override
                                public void send(Object object) throws IOException {
                                    // 收集内容
                                    if (object instanceof SseEmitter.SseEventBuilder) {
                                        // 无法直接获取内容，忽略
                                    } else if (object != null) {
                                        String content = object.toString();
                                        if (content.contains("content\":\"")) {
                                            int start = content.indexOf("content\":\"") + 10;
                                            int end = content.indexOf("\"", start);
                                            if (start > 0 && end > start) {
                                                String chunk = content.substring(start, end);
                                                contentCollector.append(chunk);
                                            }
                                        }
                                    }

                                    // 转发到真正的emitter
                                    emitter.send(object);
                                }

                                @Override
                                public void complete() {
                                    emitter.complete();
                                }

                                @Override
                                public void completeWithError(Throwable ex) {
                                    emitter.completeWithError(ex);
                                }
                            });
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void handle(Consumer<String> onMessage, Runnable onComplete, Consumer<Throwable> onError) {
                        delegate.handle(
                                message -> {
                                    // 收集内容
                                    if (message != null && message.contains("content\":\"")) {
                                        int start = message.indexOf("content\":\"") + 10;
                                        int end = message.indexOf("\"", start);
                                        if (start > 0 && end > start) {
                                            String chunk = message.substring(start, end);
                                            contentCollector.append(chunk);
                                        }
                                    }
                                    // 转发消息
                                    onMessage.accept(message);
                                },
                                onComplete,
                                onError);
                    }

                    @Override
                    public void close() throws IOException {
                        delegate.close();
                    }
                }

                // 获取流式处理器
                com.yxboot.modules.ai.provider.SSEStreamHandler streamHandler = new ContentCollectingStreamHandler(
                        aiService.findSupportedProvider(provider, model)
                                .streamingChatCompletion(provider, model, request));

                // 处理流式响应
                streamHandler.handle(emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * 从请求中提取问题内容
     * 
     * @param request 请求参数
     * @return 问题内容
     */
    private String extractQuestionContent(ModelRequestDTO request) {
        // 优先使用prompt字段作为问题
        if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
            return request.getPrompt();
        }

        // 如果没有prompt，则从messages中提取最后一条用户消息
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (int i = request.getMessages().size() - 1; i >= 0; i--) {
                var message = request.getMessages().get(i);
                if ("user".equals(message.getRole())) {
                    return message.getContent();
                }
            }
        }

        // 没有找到问题内容，返回默认值
        return "新的对话";
    }

    /**
     * 根据问题生成会话标题
     * 
     * @param question 问题内容
     * @return 会话标题
     */
    private String generateConversationTitle(String question) {
        // 限制标题长度，最多取问题的前30个字符
        int maxLength = Math.min(question.length(), 30);
        String title = question.substring(0, maxLength);

        // 如果问题超过30个字符，添加省略号
        if (question.length() > 30) {
            title += "...";
        }

        return title;
    }
}