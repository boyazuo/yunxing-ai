package com.yxboot.modules.ai.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.yxboot.ai.rag.RagPromptResult;
import com.yxboot.ai.service.RagChatPromptService;
import com.yxboot.common.api.Result;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.modules.ai.dto.ChatRequestDTO;
import com.yxboot.modules.ai.dto.ChatResponseDTO;
import com.yxboot.modules.ai.entity.Conversation;
import com.yxboot.modules.ai.entity.Message;
import com.yxboot.modules.ai.enums.MessageStatus;
import com.yxboot.modules.ai.service.ChatService;
import com.yxboot.modules.ai.service.ConversationService;
import com.yxboot.modules.ai.service.MessageService;
import com.yxboot.modules.app.entity.AppConfig;
import com.yxboot.modules.app.service.AppConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型调用控制器
 * 
 * @author Boya
 */
@Slf4j
@RestController
@RequestMapping("/v1/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI模型调用接口", description = "提供AI模型调用的相关接口")
public class ChatController {

    private final ChatService aiService;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final AppConfigService appConfigService;
    private final RagChatPromptService ragChatPromptService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "聊天模型调用", description = "调用大模型进行聊天，默认使用流式响应")
    public SseEmitter chatCompletion(@AuthenticationPrincipal SecurityUser securityUser, @RequestBody ChatRequestDTO request) throws Exception {
        Long userId = securityUser.getUserId();

        Long conversationId = handleConversation(userId, request);
        request.setConversationId(conversationId);

        Message message = messageService.createMessage(userId, request.getAppId(), conversationId, request.getPrompt());

        AppConfig appConfig = appConfigService.getByAppId(request.getAppId());
        RagPromptResult ragPrompt = ragChatPromptService.build(request.getPrompt(), appConfig);

        if (ragPrompt.getDirectResponse() != null) {
            return streamDirectResponse(conversationId, message.getMessageId(), ragPrompt.getDirectResponse());
        }

        applyRagPrompt(request, ragPrompt);

        SseEmitter emitter = new SseEmitter(300000L);
        aiService.streamingChatCompletion(request, emitter, message.getMessageId());

        return emitter;
    }

    @PostMapping("/chat/sync")
    @Operation(summary = "非流式聊天模型调用", description = "调用大模型进行聊天，使用同步响应方式")
    public Result<ChatResponseDTO> chatCompletionSync(@AuthenticationPrincipal SecurityUser securityUser, @RequestBody ChatRequestDTO request)
            throws Exception {

        Long userId = securityUser.getUserId();
        request.setStream(false);

        AppConfig appConfig = appConfigService.getByAppId(request.getAppId());
        RagPromptResult ragPrompt = ragChatPromptService.build(request.getPrompt(), appConfig);

        Long conversationId = handleConversation(userId, request);
        Message message = messageService.createMessage(userId, request.getAppId(), conversationId, request.getPrompt());

        if (ragPrompt.getDirectResponse() != null) {
            messageService.updateMessageAnswer(message.getMessageId(), ragPrompt.getDirectResponse(), MessageStatus.COMPLETED);
            ChatResponseDTO response = ChatResponseDTO.builder()
                    .content(ragPrompt.getDirectResponse())
                    .conversationId(conversationId)
                    .messageId(message.getMessageId())
                    .createTime(LocalDateTime.now())
                    .build();
            return Result.success("请求成功。", response);
        }

        applyRagPrompt(request, ragPrompt);

        ChatResponseDTO response = aiService.chatCompletion(request);

        messageService.updateMessageAnswer(message.getMessageId(), response.getContent(), MessageStatus.COMPLETED);

        response.setConversationId(conversationId);
        response.setMessageId(message.getMessageId());
        return Result.success("请求成功。", response);
    }

    private void applyRagPrompt(ChatRequestDTO request, RagPromptResult ragPrompt) {
        request.setPrompt(ragPrompt.getUserPrompt());
        request.setSystemPrompt(ragPrompt.getSystemPrompt());
    }

    private SseEmitter streamDirectResponse(Long conversationId, Long messageId, String content) throws IOException {
        SseEmitter emitter = new SseEmitter(30000L);
        messageService.updateMessageAnswer(messageId, content, MessageStatus.COMPLETED);
        emitter.send(SseEmitter.event()
                .name("metadata")
                .data(Map.of("conversationId", conversationId, "messageId", messageId)));
        emitter.send(SseEmitter.event().data(Map.of("chunk", content)));
        emitter.send(SseEmitter.event().name("end").data(""));
        emitter.complete();
        return emitter;
    }

    private Long handleConversation(Long userId, ChatRequestDTO request) {
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
}
