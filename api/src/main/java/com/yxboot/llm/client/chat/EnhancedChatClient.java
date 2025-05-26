package com.yxboot.llm.client.chat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.yxboot.llm.chat.ChatModel;
import com.yxboot.llm.chat.ChatModelFactory;
import com.yxboot.llm.chat.ChatResponse;
import com.yxboot.llm.chat.message.Message;
import com.yxboot.llm.chat.message.UserMessage;
import com.yxboot.llm.chat.prompt.Prompt;
import com.yxboot.modules.ai.entity.Provider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 增强版聊天客户端
 * 在基础ChatClient基础上添加重试机制、健康检查等高级功能
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnhancedChatClient {

    private final ChatModelFactory chatModelFactory;
    private final ChatClientConfig config;

    // 缓存已创建的ChatModel实例
    private final Map<String, CachedChatModel> modelCache = new ConcurrentHashMap<>();

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
     * 带重试的简单文本对话
     */
    public String chatWithRetry(Provider provider, String message) {
        return executeWithRetry(() -> {
            ChatModel chatModel = getChatModel(provider);
            return chatModel.call(message);
        });
    }

    /**
     * 带重试的多轮对话
     */
    public String chatWithRetry(Provider provider, List<Message> messages) {
        return executeWithRetry(() -> {
            ChatModel chatModel = getChatModel(provider);
            return chatModel.call(messages);
        });
    }

    /**
     * 带重试的流式对话
     */
    public Flux<String> streamChatWithRetry(Provider provider, String message) {
        return Flux.defer(() -> {
            ChatModel chatModel = getChatModel(provider);
            Prompt prompt = new Prompt(new UserMessage(message));
            return chatModel.stream(prompt).map(ChatResponse::getContent);
        }).retryWhen(createRetrySpec());
    }

    /**
     * 带重试的流式多轮对话
     */
    public Flux<String> streamChatWithRetry(Provider provider, List<Message> messages) {
        return Flux.defer(() -> {
            ChatModel chatModel = getChatModel(provider);
            Prompt prompt = new Prompt(messages);
            return chatModel.stream(prompt).map(ChatResponse::getContent);
        }).retryWhen(createRetrySpec());
    }

    /**
     * 异步对话
     */
    public Mono<String> chatAsync(Provider provider, String message) {
        return Mono.fromCallable(() -> {
            ChatModel chatModel = getChatModel(provider);
            return chatModel.call(message);
        }).retryWhen(createRetrySpec());
    }

    /**
     * 异步多轮对话
     */
    public Mono<String> chatAsync(Provider provider, List<Message> messages) {
        return Mono.fromCallable(() -> {
            ChatModel chatModel = getChatModel(provider);
            return chatModel.call(messages);
        }).retryWhen(createRetrySpec());
    }

    /**
     * 批量对话处理
     */
    public Flux<String> batchChat(Provider provider, List<String> messages) {
        return Flux.fromIterable(messages)
                .flatMap(message -> chatAsync(provider, message))
                .onErrorContinue((error, item) -> {
                    log.error("批量处理消息失败: {}, 错误: {}", item, error.getMessage());
                });
    }

    /**
     * 获取ChatModel实例（带健康检查）
     */
    private ChatModel getChatModel(Provider provider) {
        String cacheKey = generateCacheKey(provider);

        // 检查健康状态
        if (config.isHealthCheckEnabled() && !isProviderHealthy(provider)) {
            throw new RuntimeException("提供商 " + provider.getProviderName() + " 当前不可用");
        }

        CachedChatModel cachedModel = modelCache.computeIfAbsent(cacheKey, key -> {
            log.debug("为提供商 {} 创建新的ChatModel实例", provider.getProviderName());
            ChatModel chatModel = chatModelFactory.createChatModel(provider);
            return CachedChatModel.of(chatModel);
        });

        // 更新最后访问时间
        cachedModel.updateLastAccessTime();

        // 检查是否过期
        if (config.isCacheEnabled() && cachedModel.isExpired(config.getCacheExpireMinutes())) {
            log.debug("提供商 {} 的缓存已过期，重新创建", provider.getProviderName());
            modelCache.remove(cacheKey);
            ChatModel chatModel = chatModelFactory.createChatModel(provider);
            cachedModel = CachedChatModel.of(chatModel);
            modelCache.put(cacheKey, cachedModel);
        }

        return cachedModel.getChatModel();
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(Provider provider) {
        return provider.getProviderName() + ":" + provider.getApiKey().hashCode();
    }

    /**
     * 检查提供商是否健康
     */
    private boolean isProviderHealthy(Provider provider) {
        String cacheKey = generateCacheKey(provider);
        return healthStatusCache.getOrDefault(cacheKey, true);
    }

    /**
     * 执行带重试的操作
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        if (!config.getRetry().isEnabled()) {
            try {
                return operation.execute();
            } catch (Exception e) {
                throw new RuntimeException("操作执行失败", e);
            }
        }

        int maxAttempts = config.getRetry().getMaxAttempts();
        long delayMillis = config.getRetry().getDelayMillis();
        double multiplier = config.getRetry().getMultiplier();

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                log.warn("操作执行失败，第 {} 次尝试，错误: {}", attempt, e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep((long) (delayMillis * Math.pow(multiplier, attempt - 1)));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }

        throw new RuntimeException("操作在 " + maxAttempts + " 次重试后仍然失败", lastException);
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
                Duration.ofMillis(config.getRetry().getDelayMillis())).multiplier(config.getRetry().getMultiplier())
                .doBeforeRetry(retrySignal -> {
                    log.warn("重试操作，第 {} 次尝试，错误: {}",
                            retrySignal.totalRetries() + 1,
                            retrySignal.failure().getMessage());
                });
    }

    /**
     * 执行健康检查
     */
    private void performHealthCheck() {
        log.debug("开始执行提供商健康检查");

        modelCache.forEach((cacheKey, cachedModel) -> {
            try {
                ChatModel chatModel = cachedModel.getChatModel();
                String response = chatModel.call("ping");
                boolean isHealthy = response != null && !response.trim().isEmpty();
                healthStatusCache.put(cacheKey, isHealthy);

                if (!isHealthy) {
                    log.warn("提供商 {} 健康检查失败", cacheKey);
                }
            } catch (Exception e) {
                log.error("提供商 {} 健康检查异常: {}", cacheKey, e.getMessage());
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
                healthStatusCache.remove(entry.getKey());
            }
            return expired;
        });
    }

    /**
     * 获取健康状态统计
     */
    public Map<String, Object> getHealthStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalProviders", modelCache.size());
        stats.put("healthyProviders", healthStatusCache.values().stream().mapToInt(h -> h ? 1 : 0).sum());
        stats.put("unhealthyProviders", healthStatusCache.values().stream().mapToInt(h -> h ? 0 : 1).sum());
        stats.put("healthStatus", healthStatusCache);
        return stats;
    }

    /**
     * 可重试操作接口
     */
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}