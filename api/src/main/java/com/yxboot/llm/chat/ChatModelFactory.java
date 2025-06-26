package com.yxboot.llm.chat;

import org.springframework.stereotype.Component;
import com.yxboot.llm.provider.zhipu.ZhipuAIChatModel;
import com.yxboot.modules.ai.entity.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天模型工厂 根据提供商信息创建对应的ChatModel实例
 * 
 * 
 * @author Boya
 */
@Slf4j
@Component
public class ChatModelFactory {

    /**
     * 根据提供商创建ChatModel实例 每次调用都会创建新的实例，避免状态共享问题
     * 
     * @param provider 提供商信息
     * @return ChatModel实例
     * @throws UnsupportedOperationException 不支持的提供商
     */
    public ChatModel createChatModel(Provider provider) {
        String providerName = provider.getProviderName().toLowerCase();
        log.debug("为提供商 {} 创建ChatModel实例", providerName);

        return switch (providerName) {
            case "zhipuai", "zhipu" -> createZhipuAIChatModel(provider);
            // TODO: 添加其他提供商的实现
            // case "qianwen", "qwen" -> createQianwenChatModel(provider);
            // case "openai" -> createOpenAIChatModel(provider);
            default -> throw new UnsupportedOperationException(
                    "不支持的模型提供商: " + provider.getProviderName()
                            + "。当前支持的提供商: zhipuai");
        };
    }

    /**
     * 创建智谱AI聊天模型实例
     * 
     * @param provider 提供商信息
     * @return ZhipuAIChatModel实例
     */
    private ChatModel createZhipuAIChatModel(Provider provider) {
        // ZhipuAIApi 现在是静态工具类，直接传递给 ChatModel
        return ZhipuAIChatModel.builder()
                .apiKey(provider.getApiKey())
                .build();
    }

    // TODO: 添加其他提供商的创建方法
    // private ChatModel createQianwenChatModel(Provider provider) {
    // if (qianwenApi == null) {
    // throw new IllegalStateException("QianwenApi 未正确注入，无法创建千问聊天模型");
    // }
    //
    // return QianwenChatModel.builder()
    // .qianwenApi(qianwenApi)
    // .apiKey(provider.getApiKey())
    // .build();
    // }

    /**
     * 检查提供商是否支持
     * 
     * @param providerName 提供商名称
     * @return 是否支持
     */
    public boolean isProviderSupported(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            return false;
        }

        String normalizedName = providerName.toLowerCase();
        return switch (normalizedName) {
            case "zhipuai", "zhipu" -> true; // 总是支持，因为 API 客户端会动态创建
            // TODO: 添加其他提供商的检查
            // case "qianwen", "qwen" -> true;
            // case "openai" -> true;
            default -> false;
        };
    }

    /**
     * 获取支持的提供商列表
     * 
     * @return 支持的提供商名称列表
     */
    public java.util.List<String> getSupportedProviders() {
        java.util.List<String> supported = new java.util.ArrayList<>();

        // 添加当前支持的提供商
        supported.add("zhipuai");

        // TODO: 添加其他提供商
        // supported.add("qianwen");
        // supported.add("openai");

        return supported;
    }
}
