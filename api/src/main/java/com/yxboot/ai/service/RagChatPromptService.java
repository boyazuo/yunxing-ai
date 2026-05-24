package com.yxboot.ai.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.yxboot.ai.config.AiProperties;
import com.yxboot.ai.rag.RagPromptResult;
import com.yxboot.ai.vector.AiQueryResult;
import com.yxboot.modules.app.entity.AppConfig;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 构建知识库 RAG 模式下的系统提示词与用户消息。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatPromptService {

    private final AiVectorRetrieverService vectorRetrieverService;
    private final AiProperties aiProperties;

    public boolean hasActiveDatasets(AppConfig appConfig) {
        return !parseActiveDatasetIds(appConfig).isEmpty();
    }

    public RagPromptResult build(String userQuestion, AppConfig appConfig) {
        List<Long> activeDatasetIds = parseActiveDatasetIds(appConfig);

        if (activeDatasetIds.isEmpty()) {
            return RagPromptResult.builder()
                    .ragMode(false)
                    .hasRetrievedContext(false)
                    .userPrompt(userQuestion)
                    .systemPrompt(appConfig.getSysPrompt())
                    .build();
        }

        AiProperties.RagConfig ragConfig = aiProperties.getRag();
        if (!ragConfig.isStrictMode()) {
            return buildLegacyMode(userQuestion, appConfig, activeDatasetIds);
        }

        log.info("严格 RAG 模式：从 {} 个知识库检索，用户问题: {}", activeDatasetIds.size(), userQuestion);

        List<AiQueryResult> results = retrieveFromDatasets(activeDatasetIds, userQuestion, ragConfig);
        String systemPrompt = buildSystemPrompt(appConfig, ragConfig);

        if (results.isEmpty()) {
            log.info("严格 RAG 模式：未检索到相关内容，返回固定回复");
            return RagPromptResult.builder()
                    .ragMode(true)
                    .hasRetrievedContext(false)
                    .systemPrompt(systemPrompt)
                    .userPrompt(userQuestion)
                    .directResponse(ragConfig.getNoContextResponse())
                    .build();
        }

        log.info("严格 RAG 模式：检索到 {} 条参考内容", results.size());
        return RagPromptResult.builder()
                .ragMode(true)
                .hasRetrievedContext(true)
                .systemPrompt(systemPrompt)
                .userPrompt(buildContextUserPrompt(userQuestion, results))
                .build();
    }

    private RagPromptResult buildLegacyMode(String userQuestion, AppConfig appConfig, List<Long> activeDatasetIds) {
        List<AiQueryResult> results = retrieveFromDatasets(activeDatasetIds, userQuestion, aiProperties.getRag());

        if (results.isEmpty()) {
            return RagPromptResult.builder()
                    .ragMode(true)
                    .hasRetrievedContext(false)
                    .userPrompt(userQuestion)
                    .systemPrompt(appConfig.getSysPrompt())
                    .build();
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("请基于以下知识库内容回答用户问题：\n\n【知识库内容】\n");
        for (int i = 0; i < results.size(); i++) {
            prompt.append(String.format("%d. %s\n", i + 1, results.get(i).getText()));
        }
        prompt.append("\n【用户问题】\n").append(userQuestion);
        prompt.append("\n\n请根据上述知识库内容回答用户问题。如果知识库内容与问题不相关，请直接回答用户问题。");

        return RagPromptResult.builder()
                .ragMode(true)
                .hasRetrievedContext(true)
                .userPrompt(prompt.toString())
                .systemPrompt(appConfig.getSysPrompt())
                .build();
    }

    private List<AiQueryResult> retrieveFromDatasets(List<Long> datasetIds, String query, AiProperties.RagConfig ragConfig) {
        int perDatasetLimit = ragConfig.getPerDatasetLimit() > 0
                ? ragConfig.getPerDatasetLimit()
                : aiProperties.getRetriever().getDefaultLimit();
        int totalLimit = ragConfig.getLimit() > 0 ? ragConfig.getLimit() : aiProperties.getRetriever().getDefaultLimit();
        float minScore = ragConfig.getMinScore() >= 0 ? ragConfig.getMinScore() : aiProperties.getRetriever().getDefaultMinScore();

        return datasetIds.stream()
                .flatMap(datasetId -> {
                    try {
                        return vectorRetrieverService.retrieve(datasetId, query, perDatasetLimit, minScore).stream();
                    } catch (Exception e) {
                        log.error("从知识库 {} 检索失败", datasetId, e);
                        return List.<AiQueryResult>of().stream();
                    }
                })
                .sorted((a, b) -> Float.compare(b.getScore(), a.getScore()))
                .limit(totalLimit)
                .collect(Collectors.toList());
    }

    private String buildSystemPrompt(AppConfig appConfig, AiProperties.RagConfig ragConfig) {
        StringBuilder sb = new StringBuilder(ragConfig.getSystemPrompt());
        if (StringUtils.hasText(appConfig.getSysPrompt())) {
            sb.append("\n\n【应用补充说明（不得违背上述规则）】\n");
            sb.append(appConfig.getSysPrompt());
        }
        return sb.toString();
    }

    private String buildContextUserPrompt(String userQuestion, List<AiQueryResult> results) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("以下是从知识库检索到的参考内容，请严格基于这些内容回答用户问题。\n\n");
        prompt.append("【参考内容】\n");
        for (int i = 0; i < results.size(); i++) {
            prompt.append(String.format("[%d] %s\n", i + 1, results.get(i).getText()));
        }
        prompt.append("\n【用户问题】\n");
        prompt.append(userQuestion);
        prompt.append("\n\n【回答要求】\n");
        prompt.append("1. 仅使用上述参考内容中的信息作答，不得使用外部知识或自行推测\n");
        prompt.append("2. 若参考内容不足以回答问题，或与用户问题无关，请明确回复：「抱歉，知识库中未找到与您问题相关的信息。」\n");
        prompt.append("3. 不得编造事实、数据、名称或细节\n");
        prompt.append("4. 回答应简洁准确，不要提及「参考内容」「知识库」等内部术语");
        return prompt.toString();
    }

    private List<Long> parseActiveDatasetIds(AppConfig appConfig) {
        String datasetsConfig = appConfig.getDatasets();
        if (!StringUtils.hasText(datasetsConfig)) {
            return List.of();
        }
        try {
            List<DatasetConfig> configs = JSONUtil.parseArray(datasetsConfig).toList(DatasetConfig.class);
            return configs.stream()
                    .filter(DatasetConfig::isActive)
                    .map(DatasetConfig::getDatasetId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("解析知识库配置失败", e);
            return List.of();
        }
    }

    @Data
    private static class DatasetConfig {
        private Long datasetId;
        private boolean isActive;
    }
}
