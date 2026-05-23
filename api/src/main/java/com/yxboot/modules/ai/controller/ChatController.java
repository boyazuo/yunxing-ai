package com.yxboot.modules.ai.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.yxboot.common.api.Result;
import com.yxboot.config.security.SecurityUser;
import com.yxboot.ai.service.AiVectorRetrieverService;
import com.yxboot.ai.vector.AiQueryResult;
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
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
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
    private final AiVectorRetrieverService vectorRetrieverService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "聊天模型调用", description = "调用大模型进行聊天，默认使用流式响应")
    public SseEmitter chatCompletion(@AuthenticationPrincipal SecurityUser securityUser, @RequestBody ChatRequestDTO request) throws Exception {
        Long userId = securityUser.getUserId();

        Long conversationId = handleConversation(userId, request);
        request.setConversationId(conversationId);

        Message message = messageService.createMessage(userId, request.getAppId(), conversationId, request.getPrompt());

        AppConfig appConfig = appConfigService.getByAppId(request.getAppId());

        String enhancedPrompt = enhancePromptWithKnowledge(request.getPrompt(), appConfig);
        request.setPrompt(enhancedPrompt);
        request.setSystemPrompt(appConfig.getSysPrompt());

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

        String enhancedPrompt = enhancePromptWithKnowledge(request.getPrompt(), appConfig);
        request.setPrompt(enhancedPrompt);
        request.setSystemPrompt(appConfig.getSysPrompt());

        Long conversationId = handleConversation(userId, request);
        Message message = messageService.createMessage(userId, request.getAppId(), conversationId, request.getPrompt());

        ChatResponseDTO response = aiService.chatCompletion(request);

        messageService.updateMessageAnswer(message.getMessageId(), response.getContent(), MessageStatus.COMPLETED);

        response.setConversationId(conversationId);
        response.setMessageId(message.getMessageId());
        return Result.success("请求成功。", response);
    }

    private String enhancePromptWithKnowledge(String originalPrompt, AppConfig appConfig) {
        String datasetsConfig = appConfig.getDatasets();
        if (datasetsConfig == null || datasetsConfig.trim().isEmpty()) {
            log.debug("未配置知识库，直接使用原始prompt");
            return originalPrompt;
        }

        try {
            List<DatasetConfig> datasetConfigs = JSONUtil.parseArray(datasetsConfig).toList(DatasetConfig.class);
            List<Long> activeDatasetIds =
                    datasetConfigs.stream().filter(DatasetConfig::isActive).map(DatasetConfig::getDatasetId).collect(Collectors.toList());

            if (activeDatasetIds.isEmpty()) {
                log.debug("没有激活的知识库，直接使用原始prompt");
                return originalPrompt;
            }

            log.info("开始从知识库检索相关内容，激活的知识库数量: {}, 用户问题: {}", activeDatasetIds.size(), originalPrompt);

            List<AiQueryResult> allResults = activeDatasetIds.stream().flatMap(datasetId -> {
                try {
                    return vectorRetrieverService.retrieve(datasetId, originalPrompt, 5, 0.5f).stream();
                } catch (Exception e) {
                    log.error("从知识库 {} 检索失败", datasetId, e);
                    return List.<AiQueryResult>of().stream();
                }
            }).sorted((a, b) -> Float.compare(b.getScore(), a.getScore()))
                    .limit(10)
                    .collect(Collectors.toList());

            if (allResults.isEmpty()) {
                log.debug("未检索到相关知识库内容，直接使用原始prompt");
                return originalPrompt;
            }

            StringBuilder knowledgeContext = new StringBuilder();
            knowledgeContext.append("请基于以下知识库内容回答用户问题：\n\n");
            knowledgeContext.append("【知识库内容】\n");

            for (int i = 0; i < allResults.size(); i++) {
                AiQueryResult result = allResults.get(i);
                knowledgeContext.append(String.format("%d. %s (相似度: %.2f)\n", i + 1, result.getText(), result.getScore()));
            }

            knowledgeContext.append("\n【用户问题】\n");
            knowledgeContext.append(originalPrompt);
            knowledgeContext.append("\n\n请根据上述知识库内容回答用户问题。如果知识库内容与问题不相关，请直接回答用户问题。");

            String enhancedPrompt = knowledgeContext.toString();
            log.info("成功检索到 {} 条相关内容，增强后的prompt长度: {}", allResults.size(), enhancedPrompt.length());
            return enhancedPrompt;

        } catch (Exception e) {
            log.error("知识库检索失败，使用原始prompt", e);
            return originalPrompt;
        }
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

    @Data
    @Schema(description = "知识库配置")
    public static class DatasetConfig {
        private String id;
        private Long datasetId;
        private String name;
        private boolean isActive;
    }
}
