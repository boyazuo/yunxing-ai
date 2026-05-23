package com.yxboot.ai.registry;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.yxboot.modules.ai.entity.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * 多租户 ChatModel 注册表：按 Provider（含租户 API Key）缓存 Spring AI {@link ChatModel} 实例。
 */
@Slf4j
@Component
public class ChatModelRegistry {

    private final ConcurrentHashMap<String, ChatModel> modelCache = new ConcurrentHashMap<>();

    public ChatModel getOrCreate(Provider provider) {
        String cacheKey = buildCacheKey(provider);
        return modelCache.computeIfAbsent(cacheKey, k -> createChatModel(provider));
    }

    public void evict(Provider provider) {
        modelCache.remove(buildCacheKey(provider));
        log.debug("已清除 ChatModel 缓存, provider={}, tenant={}", provider.getProviderName(), provider.getTenantId());
    }

    private ChatModel createChatModel(Provider provider) {
        String name = provider.getProviderName().toLowerCase();
        return switch (name) {
            case "zhipuai", "zhipu" -> createZhiPuChatModel(provider);
            default -> throw new UnsupportedOperationException("不支持的模型提供商: " + provider.getProviderName());
        };
    }

    private ChatModel createZhiPuChatModel(Provider provider) {
        if (!StringUtils.hasText(provider.getApiKey())) {
            throw new IllegalArgumentException("提供商 API Key 不能为空: " + provider.getProviderName());
        }
        ZhiPuAiApi api = ZhiPuAiApi.builder()
                .apiKey(provider.getApiKey())
                .build();
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model(ZhiPuAiApi.ChatModel.GLM_4_Flash.getValue())
                .temperature(0.7)
                .build();
        return new ZhiPuAiChatModel(api, options);
    }

    private String buildCacheKey(Provider provider) {
        return String.format("%s:%d:%d",
                provider.getProviderName().toLowerCase(),
                provider.getApiKey().hashCode(),
                provider.getTenantId());
    }
}
