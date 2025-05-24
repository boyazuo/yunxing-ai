package com.yxboot.llm.chat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.yxboot.modules.ai.entity.Provider;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天模型工厂
 * 根据提供商信息选择正确的ChatModel实现
 * 
 * @author Boya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatModelFactory {

    // 所有可用的 ChatModel 实现
    private final List<ChatModel> chatModels;

    // 提供商名称到ModelProvider的映射
    private final Map<String, ModelProvider> providerMapping = Map.of(
            "zhipuai", ModelProvider.ZHIPU,
            "qianwen", ModelProvider.QIANWEN,
            "openai", ModelProvider.OPENAI,
            "azure_openai", ModelProvider.OPENAI);

    // 按ModelProvider分类的ChatModel映射
    private Map<ModelProvider, ChatModel> modelProviderMap;

    // 缓存已创建的ChatModel实例，避免重复创建
    private final Map<String, ChatModel> modelCache = new ConcurrentHashMap<>();

    /**
     * 初始化模型提供商映射
     */
    @PostConstruct
    public void initModelProviderMap() {
        modelProviderMap = chatModels.stream()
                .collect(Collectors.toMap(
                        ChatModel::getProvider,
                        model -> model,
                        (existing, replacement) -> existing));

        log.info("已初始化 {} 个 ChatModel 实现: {}",
                chatModels.size(),
                chatModels.stream().map(m -> m.getClass().getSimpleName()).toList());
    }

    /**
     * 根据提供商创建ChatModel实例
     * 
     * @param provider 提供商信息
     * @param model    模型信息
     * @return ChatModel实例
     */
    public ChatModel createChatModel(Provider provider) {
        String providerName = provider.getProviderName().toLowerCase();

        // 从缓存获取已创建的实例
        return modelCache.computeIfAbsent(providerName, key -> {
            ModelProvider targetProvider = resolveModelProvider(providerName);
            log.debug("为提供商 {} 解析出模型提供者类型: {}", providerName, targetProvider);

            // 从已注册的模型中查找并配置
            return Optional.ofNullable(modelProviderMap.get(targetProvider))
                    .map(chatModel -> configureModel(chatModel, provider))
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
    private ChatModel configureModel(ChatModel chatModel, Provider provider) {
        log.info("找到匹配的ChatModel实现: {} 用于提供商: {}",
                chatModel.getClass().getSimpleName(), provider.getProviderName());
        // 这里可以根据不同类型的模型进行特定配置
        chatModel.withApiKey(provider.getApiKey());
        return chatModel;
    }
}