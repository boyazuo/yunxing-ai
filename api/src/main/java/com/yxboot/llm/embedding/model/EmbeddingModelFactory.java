package com.yxboot.llm.embedding.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.yxboot.llm.chat.ModelProvider;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingModel;
import com.yxboot.modules.ai.entity.Provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 嵌入模型工厂
 * 根据提供商信息动态创建EmbeddingModel实例
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingModelFactory {

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

    // 缓存已创建的EmbeddingModel实例，避免重复创建
    private final Map<String, EmbeddingModel> modelCache = new ConcurrentHashMap<>();

    /**
     * 根据提供商创建EmbeddingModel实例
     * 
     * @param provider 提供商信息
     * @return EmbeddingModel实例
     */
    public EmbeddingModel createEmbeddingModel(Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("提供商信息不能为空");
        }
        String providerName = provider.getProviderName().toLowerCase();

        // 从缓存获取已创建的实例
        return modelCache.computeIfAbsent(providerName, key -> {
            ModelProvider targetProvider = resolveModelProvider(providerName);
            log.debug("为提供商 {} 解析出模型提供者类型: {}", providerName, targetProvider);

            // 根据提供商类型创建对应的EmbeddingModel实例
            return createModelByProvider(targetProvider, provider);
        });
    }

    /**
     * 根据提供商类型创建对应的EmbeddingModel实例
     * 
     * @param targetProvider 目标提供商类型
     * @param provider       提供商信息
     * @return EmbeddingModel实例
     */
    private EmbeddingModel createModelByProvider(ModelProvider targetProvider, Provider provider) {
        if (targetProvider == ModelProvider.ZHIPU) {
            return createZhipuEmbeddingModel(provider);
        } else if (targetProvider == ModelProvider.QIANWEN) {
            // TODO: 实现千问模型的创建
            throw new UnsupportedOperationException("暂不支持千问嵌入模型");
        } else if (targetProvider == ModelProvider.OPENAI) {
            // TODO: 实现OpenAI模型的创建
            throw new UnsupportedOperationException("暂不支持OpenAI嵌入模型");
        }

        throw new UnsupportedOperationException("不支持的模型提供商: " + provider.getProviderName());
    }

    /**
     * 创建智谱AI嵌入模型
     * 
     * @param provider 提供商信息
     * @return ZhipuAIEmbeddingModel实例
     */
    private EmbeddingModel createZhipuEmbeddingModel(Provider provider) {
        log.info("创建智谱AI嵌入模型, 提供商: {}", provider.getProviderName());
        return new ZhipuAIEmbeddingModel();
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
     * 清除模型缓存
     */
    public void clearCache() {
        modelCache.clear();
    }
}