package com.yxboot.ai.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

/**
 * Spring AI 基础设施配置：根据 yxboot.ai 静态配置初始化 ChatModel、EmbeddingModel 及 Qdrant 客户端。
 */
@Configuration
public class SpringAIConfig {

    @Bean
    @ConditionalOnMissingBean
    public ChatModel chatModel(AiProperties props) {
        AiProperties.ChatConfig cfg = props.getChat();
        return switch (cfg.getProvider().toLowerCase()) {
            case "zhipuai", "zhipu" -> buildZhiPuChatModel(cfg);
            default -> throw new IllegalArgumentException("不支持的 Chat 提供商: " + cfg.getProvider());
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingModel embeddingModel(AiProperties props) {
        AiProperties.EmbeddingConfig cfg = props.getEmbedding();
        return switch (cfg.getProvider().toLowerCase()) {
            case "zhipuai", "zhipu" -> buildZhiPuEmbeddingModel(cfg);
            default -> throw new IllegalArgumentException("不支持的 Embedding 提供商: " + cfg.getProvider());
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public QdrantClient qdrantClient(
            @Value("${spring.ai.vectorstore.qdrant.host:localhost}") String host,
            @Value("${spring.ai.vectorstore.qdrant.port:6334}") int port,
            @Value("${spring.ai.vectorstore.qdrant.use-tls:false}") boolean useTls,
            @Value("${spring.ai.vectorstore.qdrant.api-key:}") String apiKey) {
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(host, port, useTls);
        if (StringUtils.hasText(apiKey)) {
            builder.withApiKey(apiKey);
        }
        return new QdrantClient(builder.build());
    }

    private ChatModel buildZhiPuChatModel(AiProperties.ChatConfig cfg) {
        if (!StringUtils.hasText(cfg.getApiKey())) {
            throw new IllegalArgumentException("Chat 模型 API Key 未配置，请在 yxboot.ai.chat.api-key 中设置");
        }
        ZhiPuAiApi api = ZhiPuAiApi.builder().apiKey(cfg.getApiKey()).build();
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model(cfg.getModel())
                .temperature(cfg.getTemperature())
                .topP(cfg.getTopP())
                .maxTokens(cfg.getMaxTokens())
                .build();
        return new ZhiPuAiChatModel(api, options);
    }

    private EmbeddingModel buildZhiPuEmbeddingModel(AiProperties.EmbeddingConfig cfg) {
        if (!StringUtils.hasText(cfg.getApiKey())) {
            throw new IllegalArgumentException("Embedding 模型 API Key 未配置，请在 yxboot.ai.embedding.api-key 中设置");
        }
        ZhiPuAiApi api = ZhiPuAiApi.builder().apiKey(cfg.getApiKey()).build();
        ZhiPuAiEmbeddingOptions options = ZhiPuAiEmbeddingOptions.builder()
                .model(cfg.getModel())
                .dimensions(cfg.getDimensions())
                .build();
        return new ZhiPuAiEmbeddingModel(api, MetadataMode.EMBED, options);
    }
}
