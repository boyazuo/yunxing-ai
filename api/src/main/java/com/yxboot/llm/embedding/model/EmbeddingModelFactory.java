package com.yxboot.llm.embedding.model;

import org.springframework.stereotype.Component;
import com.yxboot.llm.chat.ModelProvider;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingConfig;
import com.yxboot.llm.provider.zhipu.ZhipuAIEmbeddingModel;
import com.yxboot.modules.ai.entity.Model;
import com.yxboot.modules.ai.entity.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * 嵌入模型工厂 根据提供商信息创建对应的EmbeddingModel实例
 * 
 * 重构说明： - 参考 ChatModelFactory 的简化设计 - 移除复杂的缓存机制和依赖注入 - 每次调用都创建新实例，避免状态共享问题 - 使用 switch 表达式提高代码可读性
 * 
 * @author Boya
 */
@Slf4j
@Component
public class EmbeddingModelFactory {

    /**
     * 根据提供商创建EmbeddingModel实例 每次调用都会创建新的实例，避免状态共享问题
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @return EmbeddingModel实例
     * @throws UnsupportedOperationException 不支持的提供商
     */
    public EmbeddingModel createEmbeddingModel(Provider provider, Model model) {
        String providerName = provider.getProviderName().toLowerCase();
        log.debug("为提供商 {} 模型 {} 创建EmbeddingModel实例", providerName, model.getModelName());

        return switch (providerName) {
            case "zhipuai", "zhipu" -> createZhipuAIEmbeddingModel(provider, model);
            // TODO: 添加其他提供商的实现
            // case "qianwen", "qwen", "tongyi" -> createQianwenEmbeddingModel(provider, model);
            // case "openai", "open_ai" -> createOpenAIEmbeddingModel(provider, model);
            default -> throw new UnsupportedOperationException(
                    "不支持的模型提供商: " + provider.getProviderName()
                            + "。当前支持的提供商: zhipuai");
        };
    }

    /**
     * 根据提供商创建EmbeddingModel实例（简化版本，不需要Model参数）
     * 
     * @param provider 提供商信息
     * @return EmbeddingModel实例
     */
    public EmbeddingModel createEmbeddingModel(Provider provider) {
        String providerName = provider.getProviderName().toLowerCase();
        log.debug("为提供商 {} 创建EmbeddingModel实例", providerName);

        return switch (providerName) {
            case "zhipuai", "zhipu" -> createZhipuAIEmbeddingModel(provider);
            // TODO: 添加其他提供商的实现
            default -> throw new UnsupportedOperationException(
                    "不支持的模型提供商: " + provider.getProviderName()
                            + "。当前支持的提供商: zhipuai");
        };
    }

    /**
     * 创建智谱AI嵌入模型实例
     * 
     * @param provider 提供商信息
     * @param model 模型信息
     * @return ZhipuAIEmbeddingModel实例
     */
    private EmbeddingModel createZhipuAIEmbeddingModel(Provider provider, Model model) {
        ZhipuAIEmbeddingConfig config = ZhipuAIEmbeddingConfig.builder()
                .apiKey(provider.getApiKey())
                .modelName(model.getModelName())
                .build();

        return ZhipuAIEmbeddingModel.builder()
                .config(config)
                .build();
    }

    /**
     * 创建智谱AI嵌入模型实例（使用默认模型）
     * 
     * @param provider 提供商信息
     * @return ZhipuAIEmbeddingModel实例
     */
    private EmbeddingModel createZhipuAIEmbeddingModel(Provider provider) {
        return ZhipuAIEmbeddingModel.builder()
                .apiKey(provider.getApiKey())
                .build();
    }

    // TODO: 添加其他提供商的创建方法
    // private EmbeddingModel createQianwenEmbeddingModel(Provider provider, Model model) {
    // return QianwenEmbeddingModel.builder()
    // .apiKey(provider.getApiKey())
    // .modelName(model.getModelName())
    // .build();
    // }

    // private EmbeddingModel createOpenAIEmbeddingModel(Provider provider, Model model) {
    // return OpenAIEmbeddingModel.builder()
    // .apiKey(provider.getApiKey())
    // .modelName(model.getModelName())
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
            case "zhipuai", "zhipu" -> true;
            // TODO: 添加其他提供商的检查
            // case "qianwen", "qwen", "tongyi" -> true;
            // case "openai", "open_ai" -> true;
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

    /**
     * 解析提供商名称为ModelProvider枚举
     * 
     * @param providerName 提供商名称
     * @return ModelProvider枚举
     */
    public ModelProvider resolveModelProvider(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            return ModelProvider.OTHER;
        }

        String normalizedName = providerName.toLowerCase();
        return switch (normalizedName) {
            case "zhipuai", "zhipu" -> ModelProvider.ZHIPU;
            case "qianwen", "qwen", "tongyi" -> ModelProvider.QIANWEN;
            case "openai", "open_ai", "azure_openai" -> ModelProvider.OPENAI;
            default -> {
                // 智能判断提供商类型
                if (normalizedName.contains("zhipu") || normalizedName.contains("chatglm")) {
                    yield ModelProvider.ZHIPU;
                } else if (normalizedName.contains("qianwen") || normalizedName.contains("qwen")
                        || normalizedName.contains("tongyi")) {
                    yield ModelProvider.QIANWEN;
                } else if (normalizedName.contains("openai") || normalizedName.contains("gpt")) {
                    yield ModelProvider.OPENAI;
                } else {
                    yield ModelProvider.OTHER;
                }
            }
        };
    }
}
