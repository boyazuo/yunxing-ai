package com.yxboot.llm.embedding.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.yxboot.llm.chat.ModelProvider;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingConfig;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingModel;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 嵌入模型工厂 根据提供商信息选择正确的EmbeddingModel实现
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingModelFactory {

    // 所有可用的 EmbeddingModel 实现
    private final List<EmbeddingModel> embeddingModels;

    // 提供商名称到ModelProvider的映射
    private final Map<String, ModelProvider> providerMapping = Map.of(
            "zhipuai", ModelProvider.ZHIPU,
            "zhipu", ModelProvider.ZHIPU,
            "qianwen", ModelProvider.QIANWEN,
            "qwen", ModelProvider.QIANWEN,
            "tongyi", ModelProvider.QIANWEN,
            "openai", ModelProvider.OPENAI,
            "open_ai", ModelProvider.OPENAI,
            "azure_openai", ModelProvider.OPENAI);

    // 按ModelProvider分类的EmbeddingModel映射
    private Map<ModelProvider, EmbeddingModel> modelProviderMap;

    // 缓存已创建的EmbeddingModel实例，避免重复创建
    private final Map<String, EmbeddingModel> modelCache = new ConcurrentHashMap<>();

    /**
     * 初始化模型提供商映射
     */
    @PostConstruct
    public void initModelProviderMap() {
        modelProviderMap = embeddingModels.stream()
                .collect(Collectors.toMap(
                        EmbeddingModel::getProvider,
                        model -> model,
                        (existing, replacement) -> existing));

        log.info("已初始化 {} 个 EmbeddingModel 实现: {}",
                embeddingModels.size(),
                embeddingModels.stream().map(m -> m.getClass().getSimpleName()).toList());
    }

    /**
     * 根据提供商和模型创建EmbeddingModel实例（推荐方法）
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @return EmbeddingModel实例
     */
    public EmbeddingModel createEmbeddingModel(Provider provider, Model model) {
        String providerName = provider.getProviderName().toLowerCase();
        String modelName = model.getModelName();

        // 生成缓存键，包含提供商和模型信息
        String cacheKey = providerName + ":" + modelName;

        // 从缓存获取已创建的实例
        return modelCache.computeIfAbsent(cacheKey, key -> {
            ModelProvider targetProvider = resolveModelProvider(providerName);
            log.debug("为提供商 {} 模型 {} 解析出模型提供者类型: {}", providerName, modelName, targetProvider);

            // 从已注册的模型中查找并配置
            return Optional.ofNullable(modelProviderMap.get(targetProvider))
                    .map(embeddingModel -> configureModel(embeddingModel, provider, model))
                    .orElseThrow(() -> new UnsupportedOperationException(
                            "不支持的模型提供商: " + provider.getProviderName()));
        });
    }

    /**
     * 解析提供商名称为ModelProvider枚举
     */
    private ModelProvider resolveModelProvider(String providerName) {
        // 直接从映射中查找
        if (providerMapping.containsKey(providerName)) {
            return providerMapping.get(providerName);
        }

        // 智能判断提供商类型
        if (providerName.contains("zhipu") || providerName.contains("chatglm")) {
            return ModelProvider.ZHIPU;
        } else if (providerName.contains("qianwen") || providerName.contains("qwen")
                || providerName.contains("tongyi")) {
            return ModelProvider.QIANWEN;
        } else if (providerName.contains("openai") || providerName.contains("gpt")) {
            return ModelProvider.OPENAI;
        }

        return ModelProvider.OTHER;
    }

    /**
     * 配置模型实例
     */
    private EmbeddingModel configureModel(EmbeddingModel embeddingModel, Provider provider, Model model) {
        log.info("找到匹配的EmbeddingModel实现: {} 用于提供商: {} 模型: {}",
                embeddingModel.getClass().getSimpleName(), provider.getProviderName(), model.getModelName());

        if (embeddingModel instanceof ZhipuAIEmbeddingModel) {
            ZhipuAIEmbeddingConfig config = ZhipuAIEmbeddingConfig.builder()
                    .apiKey(provider.getApiKey())
                    .modelName(model.getModelName())
                    .build();
            embeddingModel.configure(config);
        }

        return embeddingModel;
    }

    /**
     * 清除模型缓存
     */
    public void clearCache() {
        modelCache.clear();
    }
}
