package com.yxboot.llm.client.embedding;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.embedding.model.EmbeddingModelFactory;
import com.yxboot.llm.embedding.model.EmbeddingRequest;
import com.yxboot.llm.embedding.model.EmbeddingResponse;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 嵌入客户端 作为连接通用封装层(EmbeddingModel)和业务层的桥梁 提供简化的API调用接口，隐藏底层复杂性
 * 
 * 架构层次： 1. 大模型原生API (如 ZhipuAIApi) 2. 通用封装层 (EmbeddingModel) 3. 连接层 (EmbeddingClient) ← 当前类 4.
 * 业务逻辑层 (上层应用的业务层)
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingClient {

    private final EmbeddingModelFactory embeddingModelFactory;
    private final EmbeddingClientConfig config;

    // 缓存已创建的EmbeddingModel实例，避免重复创建
    private final Map<String, CachedEmbeddingModel> modelCache = new ConcurrentHashMap<>();

    // ========== 新的推荐方法：支持 Provider + Model ==========

    /**
     * 简单文本嵌入（推荐方法）
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @param text 输入文本
     * @return 嵌入向量
     */
    public float[] embed(Provider provider, Model model, String text) {
        EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
        return embeddingModel.embed(text);
    }

    /**
     * 批量文本嵌入（推荐方法）
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @param texts 输入文本列表
     * @return 嵌入向量列表
     */
    public List<float[]> embedAll(Provider provider, Model model, List<String> texts) {
        EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
        return embeddingModel.embedAll(texts);
    }

    /**
     * 嵌入请求处理（推荐方法）
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @param request 嵌入请求
     * @return 嵌入响应
     */
    public EmbeddingResponse embedRequest(Provider provider, Model model, EmbeddingRequest request) {
        EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
        return embeddingModel.embedRequest(request);
    }

    /**
     * 获取向量维度（推荐方法）
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @return 向量维度
     */
    public int getEmbeddingDimension(Provider provider, Model model) {
        EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
        return embeddingModel.getEmbeddingDimension();
    }

    /**
     * 健康检查（推荐方法）
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @return 是否健康
     */
    public boolean healthCheck(Provider provider, Model model) {
        try {
            // 使用简单文本进行健康检查
            String testText = "健康检查测试文本";
            float[] result = embed(provider, model, testText);
            return result != null && result.length > 0;
        } catch (Exception e) {
            log.warn("提供商 {} 模型 {} 健康检查失败: {}", provider.getProviderName(), model.getModelName(), e.getMessage());
            return false;
        }
    }

    /**
     * 获取或创建EmbeddingModel实例（推荐方法）
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @return EmbeddingModel实例
     */
    private EmbeddingModel getEmbeddingModel(Provider provider, Model model) {
        String cacheKey = generateCacheKey(provider, model);

        CachedEmbeddingModel cachedModel = modelCache.computeIfAbsent(cacheKey, key -> {
            log.debug("为提供商 {} 模型 {} 创建新的EmbeddingModel实例", provider.getProviderName(), model.getModelName());
            EmbeddingModel embeddingModel = embeddingModelFactory.createEmbeddingModel(provider, model);
            return CachedEmbeddingModel.of(embeddingModel);
        });

        // 更新最后访问时间
        cachedModel.updateLastAccessTime();

        // 检查是否过期
        if (config.isCacheEnabled() && cachedModel.isExpired(config.getCacheExpireMinutes())) {
            log.debug("提供商 {} 模型 {} 的缓存已过期，重新创建", provider.getProviderName(), model.getModelName());
            modelCache.remove(cacheKey);
            EmbeddingModel embeddingModel = embeddingModelFactory.createEmbeddingModel(provider, model);
            cachedModel = CachedEmbeddingModel.of(embeddingModel);
            modelCache.put(cacheKey, cachedModel);
        }

        return cachedModel.getEmbeddingModel();
    }

    /**
     * 生成缓存键（推荐方法）
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @return 缓存键
     */
    private String generateCacheKey(Provider provider, Model model) {
        return provider.getProviderName() + ":" + provider.getApiKey().hashCode() + ":" + model.getModelName();
    }


    /**
     * 清除指定提供商和模型的缓存
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     */
    public void clearCache(Provider provider, Model model) {
        String cacheKey = generateCacheKey(provider, model);
        modelCache.remove(cacheKey);
        log.debug("已清除提供商 {} 模型 {} 的缓存", provider.getProviderName(), model.getModelName());
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        modelCache.clear();
        log.debug("已清除所有EmbeddingModel缓存");
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cacheSize", modelCache.size());
        stats.put("maxCacheSize", config.getMaxCacheSize());
        stats.put("cacheEnabled", config.isCacheEnabled());
        stats.put("cacheExpireMinutes", config.getCacheExpireMinutes());
        return stats;
    }

    /**
     * 预热缓存
     * 
     * @param providerModelPairs 提供商-模型对列表
     */
    public void warmupCache(List<ProviderModelPair> providerModelPairs) {
        log.info("开始预热EmbeddingModel缓存，提供商-模型对数量: {}", providerModelPairs.size());

        for (ProviderModelPair pair : providerModelPairs) {
            try {
                getEmbeddingModel(pair.getProvider(), pair.getModel());
                log.debug("提供商 {} 模型 {} 缓存预热成功", pair.getProvider().getProviderName(), pair.getModel().getModelName());
            } catch (Exception e) {
                log.warn("提供商 {} 模型 {} 缓存预热失败: {}", pair.getProvider().getProviderName(),
                        pair.getModel().getModelName(), e.getMessage());
            }
        }

        log.info("EmbeddingModel缓存预热完成");
    }

    /**
     * 提供商-模型对
     */
    public static class ProviderModelPair {
        private final Provider provider;
        private final Model model;

        public ProviderModelPair(Provider provider, Model model) {
            this.provider = provider;
            this.model = model;
        }

        public Provider getProvider() {
            return provider;
        }

        public Model getModel() {
            return model;
        }

        public static ProviderModelPair of(Provider provider, Model model) {
            return new ProviderModelPair(provider, model);
        }
    }
}
