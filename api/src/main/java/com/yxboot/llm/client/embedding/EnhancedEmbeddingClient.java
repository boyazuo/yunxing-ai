package com.yxboot.llm.client.embedding;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import com.yxboot.llm.embedding.model.EmbeddingModel;
import com.yxboot.llm.embedding.model.EmbeddingModelFactory;
import com.yxboot.llm.embedding.model.EmbeddingRequest;
import com.yxboot.llm.embedding.model.EmbeddingResponse;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 增强版嵌入客户端 在基础EmbeddingClient基础上添加重试机制、健康检查等高级功能
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnhancedEmbeddingClient {

    private final EmbeddingModelFactory embeddingModelFactory;
    private final EmbeddingClientConfig config;

    // 缓存已创建的EmbeddingModel实例
    private final Map<String, CachedEmbeddingModel> modelCache = new ConcurrentHashMap<>();

    // 提供商健康状态缓存
    private final Map<String, Boolean> healthStatusCache = new ConcurrentHashMap<>();

    // 定时任务执行器
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        if (config.isHealthCheckEnabled()) {
            scheduler = Executors.newScheduledThreadPool(2);
            // 启动定期健康检查
            scheduler.scheduleAtFixedRate(this::performHealthCheck,
                    0, config.getHealthCheckIntervalMinutes(), TimeUnit.MINUTES);
            // 启动定期缓存清理
            scheduler.scheduleAtFixedRate(this::cleanExpiredCache,
                    config.getCacheExpireMinutes(), config.getCacheExpireMinutes(), TimeUnit.MINUTES);
        }
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 带重试的简单文本嵌入
     */
    public float[] embedWithRetry(Provider provider, Model model, String text) {
        return executeWithRetry(() -> {
            EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
            return embeddingModel.embed(text);
        });
    }

    /**
     * 带重试的批量文本嵌入
     */
    public List<float[]> embedAllWithRetry(Provider provider, Model model, List<String> texts) {
        return executeWithRetry(() -> {
            EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
            return embeddingModel.embedAll(texts);
        });
    }

    /**
     * 带重试的嵌入请求处理
     */
    public EmbeddingResponse embedRequestWithRetry(Provider provider, Model model, EmbeddingRequest request) {
        return executeWithRetry(() -> {
            EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
            return embeddingModel.embedRequest(request);
        });
    }

    /**
     * 异步文本嵌入
     */
    public Mono<float[]> embedAsync(Provider provider, Model model, String text) {
        return Mono.fromCallable(() -> {
            EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
            return embeddingModel.embed(text);
        }).retryWhen(createRetrySpec());
    }

    /**
     * 异步批量文本嵌入
     */
    public Mono<List<float[]>> embedAllAsync(Provider provider, Model model, List<String> texts) {
        return Mono.fromCallable(() -> {
            EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
            return embeddingModel.embedAll(texts);
        }).retryWhen(createRetrySpec());
    }

    /**
     * 异步嵌入请求处理
     */
    public Mono<EmbeddingResponse> embedRequestAsync(Provider provider, Model model, EmbeddingRequest request) {
        return Mono.fromCallable(() -> {
            EmbeddingModel embeddingModel = getEmbeddingModel(provider, model);
            return embeddingModel.embedRequest(request);
        }).retryWhen(createRetrySpec());
    }

    /**
     * 健康检查
     */
    public boolean healthCheck(Provider provider, Model model) {
        String cacheKey = generateCacheKey(provider);

        // 检查缓存的健康状态
        Boolean cachedStatus = healthStatusCache.get(cacheKey);
        if (cachedStatus != null) {
            return cachedStatus;
        }

        // 执行实际健康检查
        boolean isHealthy = performHealthCheck(provider, model);
        healthStatusCache.put(cacheKey, isHealthy);

        return isHealthy;
    }

    /**
     * 获取提供商健康状态
     */
    public Map<String, Boolean> getAllHealthStatus() {
        return new ConcurrentHashMap<>(healthStatusCache);
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("modelCacheSize", modelCache.size());
        stats.put("healthCacheSize", healthStatusCache.size());
        stats.put("maxCacheSize", config.getMaxCacheSize());
        stats.put("cacheEnabled", config.isCacheEnabled());
        stats.put("cacheExpireMinutes", config.getCacheExpireMinutes());
        return stats;
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        modelCache.clear();
        healthStatusCache.clear();
        log.info("已清除所有EmbeddingModel缓存");
    }

    /**
     * 执行带重试的操作
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation) {
        if (!config.getRetry().isEnabled()) {
            return operation.get();
        }

        Exception lastException = null;
        int attempts = 0;
        long delay = config.getRetry().getDelayMillis();

        while (attempts < config.getRetry().getMaxAttempts()) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempts++;

                if (attempts >= config.getRetry().getMaxAttempts()) {
                    break;
                }

                log.warn("操作失败，第 {} 次重试，延迟 {} ms: {}", attempts, delay, e.getMessage());

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }

                delay = (long) (delay * config.getRetry().getMultiplier());
            }
        }

        throw new RuntimeException("操作失败，已达到最大重试次数 " + config.getRetry().getMaxAttempts(), lastException);
    }

    /**
     * 创建重试规范
     */
    private Retry createRetrySpec() {
        if (!config.getRetry().isEnabled()) {
            return Retry.max(0);
        }

        return Retry.backoff(
                config.getRetry().getMaxAttempts(),
                Duration.ofMillis(config.getRetry().getDelayMillis())).multiplier(config.getRetry().getMultiplier());
    }

    /**
     * 获取或创建EmbeddingModel实例
     */
    private EmbeddingModel getEmbeddingModel(Provider provider, Model model) {
        String cacheKey = generateCacheKey(provider);

        CachedEmbeddingModel cachedModel = modelCache.computeIfAbsent(cacheKey, key -> {
            log.debug("为提供商 {} 创建新的EmbeddingModel实例", provider.getProviderName());
            EmbeddingModel embeddingModel = embeddingModelFactory.createEmbeddingModel(provider, model);
            embeddingModel.withApiKey(provider.getApiKey());
            return CachedEmbeddingModel.of(embeddingModel);
        });

        // 更新最后访问时间
        cachedModel.updateLastAccessTime();

        // 检查是否过期
        if (config.isCacheEnabled() && cachedModel.isExpired(config.getCacheExpireMinutes())) {
            log.debug("提供商 {} 的缓存已过期，重新创建", provider.getProviderName());
            modelCache.remove(cacheKey);
            EmbeddingModel embeddingModel = embeddingModelFactory.createEmbeddingModel(provider, model);
            embeddingModel.withApiKey(provider.getApiKey());
            cachedModel = CachedEmbeddingModel.of(embeddingModel);
            modelCache.put(cacheKey, cachedModel);
        }

        return cachedModel.getEmbeddingModel();
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(Provider provider) {
        return provider.getProviderName() + ":" + provider.getApiKey().hashCode();
    }

    /**
     * 执行健康检查
     */
    private boolean performHealthCheck(Provider provider, Model model) {
        try {
            String testText = "健康检查测试文本";
            float[] result = getEmbeddingModel(provider, model).embed(testText);
            return result != null && result.length > 0;
        } catch (Exception e) {
            log.warn("提供商 {} 健康检查失败: {}", provider.getProviderName(), e.getMessage());
            return false;
        }
    }

    /**
     * 定期健康检查
     */
    private void performHealthCheck() {
        log.debug("开始定期健康检查");

        modelCache.keySet().forEach(cacheKey -> {
            try {
                // 从缓存键解析提供商信息（简化实现）
                // 实际项目中可能需要更复杂的逻辑
                CachedEmbeddingModel cachedModel = modelCache.get(cacheKey);
                if (cachedModel != null) {
                    // 这里简化处理，实际应该根据缓存键重建Provider对象
                    log.debug("健康检查缓存键: {}", cacheKey);
                }
            } catch (Exception e) {
                log.warn("健康检查失败，缓存键: {}, 错误: {}", cacheKey, e.getMessage());
                healthStatusCache.put(cacheKey, false);
            }
        });
    }

    /**
     * 清理过期缓存
     */
    private void cleanExpiredCache() {
        log.debug("开始清理过期缓存");

        modelCache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired(config.getCacheExpireMinutes());
            if (expired) {
                log.debug("清理过期缓存: {}", entry.getKey());
            }
            return expired;
        });

        log.debug("缓存清理完成，当前缓存大小: {}", modelCache.size());
    }
}
