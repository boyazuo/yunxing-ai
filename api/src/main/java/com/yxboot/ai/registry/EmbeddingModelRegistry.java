package com.yxboot.ai.registry;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * 多租户 EmbeddingModel 注册表。
 */
@Slf4j
@Component
public class EmbeddingModelRegistry {

    private static final int DEFAULT_EMBEDDING_DIMENSION = 2048;

    private final ConcurrentHashMap<String, EmbeddingModel> modelCache = new ConcurrentHashMap<>();

    public EmbeddingModel getOrCreate(Provider provider, Model model) {
        String cacheKey = buildCacheKey(provider, model);
        return modelCache.computeIfAbsent(cacheKey, k -> createEmbeddingModel(provider, model));
    }

    public int getEmbeddingDimension(Provider provider, Model model) {
        EmbeddingModel embeddingModel = getOrCreate(provider, model);
        return embeddingModel.dimensions();
    }

    public void evict(Provider provider, Model model) {
        modelCache.remove(buildCacheKey(provider, model));
    }

    private EmbeddingModel createEmbeddingModel(Provider provider, Model model) {
        String name = provider.getProviderName().toLowerCase();
        return switch (name) {
            case "zhipuai", "zhipu" -> createZhiPuEmbeddingModel(provider, model);
            default -> throw new UnsupportedOperationException("不支持的 Embedding 提供商: " + provider.getProviderName());
        };
    }

    private EmbeddingModel createZhiPuEmbeddingModel(Provider provider, Model model) {
        if (!StringUtils.hasText(provider.getApiKey())) {
            throw new IllegalArgumentException("提供商 API Key 不能为空: " + provider.getProviderName());
        }
        ZhiPuAiApi api = ZhiPuAiApi.builder()
                .apiKey(provider.getApiKey())
                .build();
        String modelName = StringUtils.hasText(model.getModelName()) ? model.getModelName() : "embedding-3";
        ZhiPuAiEmbeddingOptions options = ZhiPuAiEmbeddingOptions.builder()
                .model(modelName)
                .dimensions(DEFAULT_EMBEDDING_DIMENSION)
                .build();
        return new ZhiPuAiEmbeddingModel(api, MetadataMode.EMBED, options);
    }

    private String buildCacheKey(Provider provider, Model model) {
        return String.format("%s:%s:%d",
                provider.getProviderName().toLowerCase(),
                model.getModelName(),
                provider.getTenantId());
    }
}
