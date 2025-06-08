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
import com.yxboot.llm.client.vector.VectorRetrieverClient;
import com.yxboot.llm.vector.query.QueryResult;
import com.yxboot.modules.ai.dto.ChatRequestDTO;
import com.yxboot.modules.ai.dto.ChatResponseDTO;
import com.yxboot.modules.ai.entity.Conversation;
import com.yxboot.modules.ai.entity.Message;
import com.yxboot.modules.ai.entity.Provider;
import com.yxboot.modules.ai.enums.MessageStatus;
import com.yxboot.modules.ai.service.ChatService;
import com.yxboot.modules.ai.service.ConversationService;
import com.yxboot.modules.ai.service.MessageService;
import com.yxboot.modules.ai.service.ProviderService;
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
    private final ProviderService providerService;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final AppConfigService appConfigService;
    private final VectorRetrieverClient vectorRetrieverClient;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "聊天模型调用", description = "调用大模型进行聊天，默认使用流式响应")
    public SseEmitter chatCompletion(@AuthenticationPrincipal SecurityUser securityUser, @RequestBody ChatRequestDTO request) throws Exception {
        Long userId = securityUser.getUserId();

        // 处理会话
        Long conversationId = handleConversation(userId, request);
        request.setConversationId(conversationId);

        // 创建消息记录
        Message message = messageService.createMessage(userId, request.getAppId(), conversationId, request.getPrompt());

        // 获取提供商信息
        Provider provider = providerService.getProviderByModelId(request.getModelId());

        // 获取应用配置
        AppConfig appConfig = appConfigService.getByAppId(request.getAppId());

        // 处理知识库检索并增强prompt
        String enhancedPrompt = enhancePromptWithKnowledge(request.getPrompt(), appConfig);
        request.setPrompt(enhancedPrompt);

        // 设置系统提示词
        request.setSystemPrompt(appConfig.getSysPrompt());
        // 获取模型配置
        String models = appConfig.getModels();
        if (models != null && !models.isEmpty()) {
            List<ModelConfig> modelConfigs = JSONUtil.parseArray(models).toList(ModelConfig.class);
            ModelConfig modelConfig = modelConfigs.stream().filter(m -> m.getModelId().equals(request.getModelId())).findFirst().orElse(null);
            if (modelConfig != null) {
                request.setModelId(modelConfig.getModelId());
                request.setModelName(modelConfig.getModelName());
                request.setTemperature(Float.parseFloat(modelConfig.getTemperature()));
                request.setTopP(Float.parseFloat(modelConfig.getTopP()));
                request.setMaxTokens(Integer.parseInt(modelConfig.getMaxTokens()));
            }
        }

        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 调用AI服务进行流式聊天
        aiService.streamingChatCompletion(provider, request, emitter, message.getMessageId());

        return emitter;
    }

    /**
     * 非流式聊天模型调用
     * 
     * @param securityUser 当前用户
     * @param request 请求参数
     * @return 模型响应
     * @throws Exception 调用异常
     */
    @PostMapping("/chat/sync")
    @Operation(summary = "非流式聊天模型调用", description = "调用大模型进行聊天，使用同步响应方式")
    public Result<ChatResponseDTO> chatCompletionSync(@AuthenticationPrincipal SecurityUser securityUser, @RequestBody ChatRequestDTO request)
            throws Exception {

        Long userId = securityUser.getUserId();
        // 确保使用非流式请求
        request.setStream(false);

        // 获取提供商信息
        Provider provider = providerService.getProviderByModelId(request.getModelId());
        // 获取应用配置
        AppConfig appConfig = appConfigService.getByAppId(request.getAppId());

        // 处理知识库检索并增强prompt
        String enhancedPrompt = enhancePromptWithKnowledge(request.getPrompt(), appConfig);
        request.setPrompt(enhancedPrompt);

        // 设置系统提示词
        request.setSystemPrompt(appConfig.getSysPrompt());

        // 获取模型配置
        String models = appConfig.getModels();
        if (models != null && !models.isEmpty()) {
            List<ModelConfig> modelConfigs = JSONUtil.parseArray(models).toList(ModelConfig.class);
            ModelConfig modelConfig = modelConfigs.stream().filter(m -> m.getModelId().equals(request.getModelId())).findFirst().orElse(null);
            if (modelConfig != null) {
                request.setModelId(modelConfig.getModelId());
                request.setModelName(modelConfig.getModelName());
                request.setTemperature(Float.parseFloat(modelConfig.getTemperature()));
                request.setTopP(Float.parseFloat(modelConfig.getTopP()));
                request.setMaxTokens(Integer.parseInt(modelConfig.getMaxTokens()));
            }
        }

        // 处理会话
        Long conversationId = handleConversation(userId, request);

        // 创建消息记录
        Message message = messageService.createMessage(userId, request.getAppId(), conversationId, request.getPrompt());

        // 调用AI服务进行聊天
        ChatResponseDTO response = aiService.chatCompletion(provider, request);

        // 更新消息回复
        messageService.updateMessageAnswer(message.getMessageId(), response.getContent(), MessageStatus.COMPLETED);

        response.setConversationId(conversationId);
        response.setMessageId(message.getMessageId());
        return Result.success("请求成功。", response);
    }

    /**
     * 使用知识库检索增强用户问题的prompt
     * 
     * @param originalPrompt 原始用户问题
     * @param appConfig 应用配置
     * @return 增强后的prompt
     */
    private String enhancePromptWithKnowledge(String originalPrompt, AppConfig appConfig) {
        // 检查是否配置了知识库
        String datasetsConfig = appConfig.getDatasets();
        if (datasetsConfig == null || datasetsConfig.trim().isEmpty()) {
            log.debug("未配置知识库，直接使用原始prompt");
            return originalPrompt;
        }

        try {
            // 解析知识库配置
            List<DatasetConfig> datasetConfigs = JSONUtil.parseArray(datasetsConfig).toList(DatasetConfig.class);
            // 过滤出激活的知识库
            List<Long> activeDatasetIds =
                    datasetConfigs.stream().filter(DatasetConfig::isActive).map(DatasetConfig::getDatasetId).collect(Collectors.toList());

            if (activeDatasetIds.isEmpty()) {
                log.debug("没有激活的知识库，直接使用原始prompt");
                return originalPrompt;
            }

            log.info("开始从知识库检索相关内容，激活的知识库数量: {}, 用户问题: {}", activeDatasetIds.size(), originalPrompt);

            // 从多个知识库中检索相关内容
            List<QueryResult> allResults = activeDatasetIds.stream().flatMap(datasetId -> {
                try {
                    // 每个知识库最多检索5条相关内容，相似度阈值0.5
                    return vectorRetrieverClient.retrieve(datasetId, originalPrompt).stream();
                } catch (Exception e) {
                    log.error("从知识库 {} 检索失败", datasetId, e);
                    return List.<QueryResult>of().stream();
                }
            }).sorted((a, b) -> Float.compare(b.getScore(), a.getScore())) // 按相似度降序排列
                    .limit(10) // 总共最多取10条最相关的内容
                    .collect(Collectors.toList());

            if (allResults.isEmpty()) {
                log.debug("未检索到相关知识库内容，直接使用原始prompt");
                return originalPrompt;
            }

            // 构建知识库上下文
            StringBuilder knowledgeContext = new StringBuilder();
            knowledgeContext.append("请基于以下知识库内容回答用户问题：\n\n");
            knowledgeContext.append("【知识库内容】\n");

            for (int i = 0; i < allResults.size(); i++) {
                QueryResult result = allResults.get(i);
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

    /**
     * 处理会话逻辑 如果conversationId为空，创建新会话 如果conversationId存在，更新会话时间
     * 
     * @param request 请求参数
     * @return 会话ID
     */
    private Long handleConversation(Long userId, ChatRequestDTO request) {
        Long conversationId = null;

        // 检查是否存在会话ID
        if (request.getConversationId() != null) {

            conversationId = request.getConversationId();
            // 检查会话是否存在
            Conversation conversation = conversationService.getById(conversationId);
            if (conversation != null) {
                // 更新会话的更新时间
                conversation.setUpdateTime(LocalDateTime.now());
                conversationService.updateById(conversation);
                return conversationId;
            }
        }

        // 创建新会话
        String title = request.getPrompt();
        // 限制标题长度
        if (title.length() > 50) {
            title = title.substring(0, 47) + "...";
        }

        Conversation conversation = conversationService.createConversation(userId, request.getAppId(), title);

        return conversation.getConversationId();
    }

    @Data
    @Schema(description = "模型配置")
    public static class ModelConfig {
        private String id;
        private Long modelId;
        private String modelName;
        private String provider;
        private String temperature;
        private String topP;
        private String maxTokens;
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
