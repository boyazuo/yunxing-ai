package com.yxboot.modules.ai.controller;

import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.yxboot.common.api.Result;
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
    private final ProviderService providerService;
    private final ModelService modelService;
    private final ConversationService conversationService;
    private final MessageService messageService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "聊天模型调用", description = "调用大模型进行聊天，默认使用流式响应")
    public SseEmitter chatCompletion(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody ModelRequestDTO request) throws Exception {

        Long userId = securityUser.getUserId();
        // 获取提供商和模型信息
        Model model = modelService.getById(request.getModelId());
        Provider provider = providerService.getById(model.getProviderId());

        // 处理会话
        Long conversationId = handleConversation(userId, request);
        request.setConversationId(conversationId.toString());

        // 创建消息记录
        Message message = messageService.createMessage(
                userId,
                request.getAppId(),
                conversationId,
                request.getPrompt());

        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 调用AI服务进行流式聊天
        aiService.streamingChatCompletion(provider, model, request, emitter, message.getMessageId());

        return emitter;
    }

    /**
     * 非流式聊天模型调用
     * 
     * @param securityUser 当前用户
     * @param request      请求参数
     * @return 模型响应
     * @throws Exception 调用异常
     */
    @PostMapping("/chat/sync")
    @Operation(summary = "非流式聊天模型调用", description = "调用大模型进行聊天，使用同步响应方式")
    public Result<ModelResponseDTO> chatCompletionSync(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody ModelRequestDTO request) throws Exception {

        Long userId = securityUser.getUserId();
        // 确保使用非流式请求
        request.setStream(false);

        // 获取提供商和模型信息
        Model model = modelService.getById(request.getModelId());
        Provider provider = providerService.getById(model.getProviderId());

        // 处理会话
        Long conversationId = handleConversation(userId, request);

        // 创建消息记录
        Message message = messageService.createMessage(
                userId,
                request.getAppId(),
                conversationId,
                request.getPrompt());

        // 调用AI服务进行聊天
        ModelResponseDTO response = aiService.chatCompletion(provider, model, request);

        // 更新消息回复
        messageService.updateMessageAnswer(message.getMessageId(), response.getContent(), MessageStatus.COMPLETED);

        response.setConversationId(conversationId);
        response.setMessageId(message.getMessageId());
        return Result.success("请求成功。", response);
    }

    /**
     * 处理会话逻辑
     * 如果conversationId为空，创建新会话
     * 如果conversationId存在，更新会话时间
     * 
     * @param request 请求参数
     * @return 会话ID
     */
    private Long handleConversation(Long userId, ModelRequestDTO request) {
        Long conversationId = null;

        // 检查是否存在会话ID
        if (request.getConversationId() != null && !request.getConversationId().isEmpty()) {
            try {
                conversationId = Long.parseLong(request.getConversationId());

                // 检查会话是否存在
                Conversation conversation = conversationService.getById(conversationId);
                if (conversation != null) {
                    // 更新会话的更新时间
                    conversation.setUpdateTime(LocalDateTime.now());
                    conversationService.updateById(conversation);
                    return conversationId;
                }
            } catch (NumberFormatException e) {
                // conversationId不是有效的Long，忽略并创建新会话
            }
        }

        // 创建新会话
        String title = request.getPrompt();
        // 限制标题长度
        if (title.length() > 50) {
            title = title.substring(0, 47) + "...";
        }

        Conversation conversation = conversationService.createConversation(
                userId,
                request.getAppId(),
                title);

        return conversation.getConversationId();
    }
}